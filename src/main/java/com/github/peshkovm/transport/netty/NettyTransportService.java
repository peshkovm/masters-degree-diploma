package com.github.peshkovm.transport.netty;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.netty.NettyClient;
import com.github.peshkovm.common.netty.NettyProvider;
import com.github.peshkovm.diagram.DiagramArrowCodec;
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
import io.netty.handler.logging.LoggingHandler;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.concurrent.Future;
import io.vavr.concurrent.Promise;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Netty {@link TransportService} implementation. */
@Component
public class NettyTransportService extends NettyClient implements TransportService {

  private final DiagramArrowCodec diagramArrowCodec;
  private volatile Map<DiscoveryNode, Channel> connectedNodes = HashMap.empty();
  private final ReentrantLock connectionLock = new ReentrantLock();
  private final TransportController transportController;

  /**
   * Constructs a new instance using provider for bootstrapping.
   *
   * @param provider provides SocketChannel and EventLoopGroup
   * @param transportController transport controller to dispatch messages
   */
  @Autowired
  public NettyTransportService(
      NettyProvider provider,
      TransportController transportController,
      DiagramArrowCodec diagramArrowCodec) {
    super(provider);
    this.transportController = transportController;
    this.diagramArrowCodec = diagramArrowCodec;
  }

  @Override
  protected ChannelInitializer<Channel> channelInitializer() {
    return new ClientChannelInitializer();
  }

  private class ClientChannelInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
      final ChannelPipeline pipeline = ch.pipeline();
      pipeline.addLast(
          new LoggingHandler(
              LoggingHandler.class.getName() + "." + this.getClass().getSimpleName() + ".Channel"));
      pipeline.addLast(new ObjectEncoder());
      pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
      pipeline.addLast(diagramArrowCodec);
      pipeline.addLast(/*provider.getExecutor(),*/ new TransportClientHandler());
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

      if (connectedNodes.getOrElse(discoveryNode, null) != null) { // already connected to node
        return;
      }
      connectionLock.lock();
      try {
        if (connectedNodes.getOrElse(discoveryNode, null) != null) { // already connected to node
          return;
        }

        final Channel channel =
            bootstrap.connect(discoveryNode.getHost(), discoveryNode.getPort()).sync().channel();
        connectedNodes = connectedNodes.put(discoveryNode, channel);

        logger.debug("Connected to {}", () -> discoveryNode);
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
      final Channel channel = connectedNodes.getOrElse(discoveryNode, null);
      if (channel == null) { // Not connected
        return;
      }
      channel.close().sync();
      connectedNodes = connectedNodes.remove(discoveryNode);
    } catch (InterruptedException e) {
      logger.error("Error disconnecting from {}", discoveryNode, e);
      Thread.currentThread().interrupt();
      e.printStackTrace();
    } finally {
      connectionLock.unlock();
    }
  }

  @Override
  public DiscoveryFuture send(DiscoveryNode discoveryNode, Message message) {
    Promise<DiscoveryNode> sendPromise = Promise.make();

    try {
      logger.debug("Sending {} to node {}...", message, discoveryNode);
      final ChannelFuture future = getChannel(discoveryNode).writeAndFlush(message);
      future.addListener(
          FIRE_EXCEPTION_ON_FAILURE); // Let object serialisation exceptions propagate.

      return new DiscoveryFuture(discoveryNode, sendPromise.success(null).future());
    } catch (Exception e) {
      logger.error("Error send message", e);
      return new DiscoveryFuture(discoveryNode, sendPromise.failure(e).future());
    }
  }

  private Channel getChannel(DiscoveryNode node) {
    Preconditions.checkNotNull(node);
    final Channel channel = connectedNodes.getOrElse(node, null);
    if (channel == null) {
      throw new IllegalArgumentException("Not connected to node: " + node);
    }
    return channel;
  }

  @Data
  public static class DiscoveryFuture {

    private final DiscoveryNode discoveryNode;

    private final Future<? extends DiscoveryNode> future;

    public DiscoveryFuture(DiscoveryNode discoveryNode, Future<? extends DiscoveryNode> future) {
      this.discoveryNode = discoveryNode;
      this.future = future;
    }
  }

  @Override
  protected void doStop() {
    connectedNodes.keySet().forEach(this::disconnectFromNode);
    super.doStop();
  }
}
