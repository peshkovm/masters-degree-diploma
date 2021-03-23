package com.github.peshkovm.transport.netty;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.netty.NettyClient;
import com.github.peshkovm.common.netty.NettyProvider;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportController;
import com.github.peshkovm.transport.TransportService;
import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Netty {@link TransportService} implementation.
 */
@Component
public class NettyTransportService extends NettyClient implements TransportService {

  private volatile Map<DiscoveryNode, Channel> connectedNodes = new ConcurrentHashMap<>();
  private final ReentrantLock connectionLock = new ReentrantLock();
  private final TransportController transportController;

  /**
   * Constructs a new instance using provider for bootstrapping.
   *
   * @param provider provides SocketChannel and EventLoopGroup
   * @param transportController transport controller to dispatch messages
   */
  @Autowired
  public NettyTransportService(NettyProvider provider, TransportController transportController) {
    super(provider);
    this.transportController = transportController;
  }

  @Override
  protected ChannelInitializer<Channel> channelInitializer() {
    return new ClientChannelInitializer();
  }

  private class ClientChannelInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
      final ChannelPipeline pipeline = ch.pipeline();
      pipeline.addLast(new ObjectEncoder());
      pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
      pipeline.addLast(executor, new TransportClientHandler());
    }
  }

  private class TransportClientHandler extends SimpleChannelInboundHandler<Message> {

    protected TransportClientHandler() {
      super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
      transportController.dispatch(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      logger.error("Unexpected channel error, closing channel...", cause);
      ctx.close();
    }
  }

  @Override
  public void connectToNode(DiscoveryNode discoveryNode) {
    if (isStarted()) {
      Preconditions.checkNotNull(discoveryNode);

      if (connectedNodes.get(discoveryNode) != null) { // already connected to node
        return;
      }
      connectionLock.lock();
      try {
        if (connectedNodes.get(discoveryNode) != null) { // already connected to node
          return;
        }

        final Channel channel =
            bootstrap.connect(discoveryNode.getHost(), discoveryNode.getPort()).sync().channel();
        connectedNodes.put(discoveryNode, channel);

        logger.info("Connected to {}", () -> discoveryNode);
      } catch (InterruptedException e) {
        logger.error("Error connecting to {}", discoveryNode, e);
        Thread.currentThread().interrupt();
        e.printStackTrace();
      } finally {
        connectionLock.unlock();
      }
    }
  }

  @Override
  public void disconnectFromNode(DiscoveryNode discoveryNode) {
    Preconditions.checkNotNull(discoveryNode);
    connectionLock.lock();
    try {
      final Channel channel = connectedNodes.getOrDefault(discoveryNode, null);
      if (channel == null) { // Not connected
        return;
      }
      channel.close().sync();
      connectedNodes.remove(discoveryNode);
    } catch (InterruptedException e) {
      logger.error("Error disconnecting from {}", discoveryNode, e);
      Thread.currentThread().interrupt();
      e.printStackTrace();
    } finally {
      connectionLock.unlock();
    }
  }

  @Override
  public void send(DiscoveryNode discoveryNode, Message message) {
    try {
      connectToNode(discoveryNode);
      logger.debug("Sending {} to node {}...", message, discoveryNode);
      final ChannelFuture future = getChannel(discoveryNode).writeAndFlush(message);
      future.addListener(
          FIRE_EXCEPTION_ON_FAILURE); // Let object serialisation exceptions propagate.
    } catch (Exception e) {
      logger.error("Error send message", e);
    }
  }

  private Channel getChannel(DiscoveryNode node) {
    Preconditions.checkNotNull(node);
    final Channel channel = connectedNodes.getOrDefault(node, null);
    if (channel == null) {
      throw new IllegalArgumentException("Not connected to node: " + node);
    }
    return channel;
  }

  @Override
  protected void doStop() {
    connectedNodes.keySet().forEach(this::disconnectFromNode);
    super.doStop();
  }
}
