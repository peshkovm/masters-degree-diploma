package com.github.peshkovm.transport.netty;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.diagram.DiagramBuilderSingleton;
import com.github.peshkovm.common.diagram.DrawIOColor;
import com.github.peshkovm.common.diagram.MxCellPojo;
import com.github.peshkovm.common.diagram.NodeMessagePair;
import com.github.peshkovm.common.netty.NettyClient;
import com.github.peshkovm.common.netty.NettyProvider;
import com.github.peshkovm.crdt.commutative.protocol.DownstreamUpdate;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Netty {@link TransportService} implementation. */
@Component
public class NettyTransportService extends NettyClient implements TransportService {

  private final DiscoveryNode self;
  private volatile Map<DiscoveryNode, Channel> connectedNodes = HashMap.empty();
  private final ReentrantLock connectionLock = new ReentrantLock();
  private final TransportController transportController;
  private final DiagramBuilderSingleton diagramBuilder;

  private final java.util.Map<Message, MxCellPojo> messageArrowMap;

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
      DiagramBuilderSingleton diagramBuilder,
      ClusterDiscovery discovery) {
    super(provider);
    this.transportController = transportController;
    this.messageArrowMap = new ConcurrentHashMap<>();
    this.diagramBuilder = diagramBuilder;
    self = discovery.getSelf();
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
      pipeline.addLast(provider.getExecutor(), new TransportClientHandler());
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
  public MyChannelFuture<Void> send(DiscoveryNode discoveryNode, Message message) {
    Promise<DiscoveryNode> sendPromise = Promise.make();

    try {
      logger.debug("Sending {} to node {}...", message, discoveryNode);

      if (diagramBuilder.isActive()) {
        String arrowName = "";

        String arrowNameColor = DrawIOColor.WHITE.fillColor;

        //        if (message instanceof ClientMessage) {
        //          if (((ClientMessage) message).getMessage().getCommand() instanceof AddResource)
        // {
        //            arrowName = ((ClientMessage) message).getMessage().getCommand().toString();
        //          }
        //        } else if (message instanceof ClientMessageSuccessful) {
        //          if (((ClientMessageSuccessful) message).getCommandResult().isSuccessful()) {
        //            arrowNameColor = DrawIOColor.GREEN.strokeColor;
        //          } else {
        //            arrowNameColor = DrawIOColor.RED.strokeColor;
        //          }
        //
        //          arrowName = ((ClientMessageSuccessful)
        // message).getCommandResult().getResult().toString();
        //        } else if (message instanceof DownstreamUpdate) {
        //          final String simpleName =
        //              ((DownstreamUpdate<?, ?>) message).getCrdtType().getSimpleName();
        //          final String crdtId = ((DownstreamUpdate<?, ?>) message).getCrdtId();
        //          final String argument = ((DownstreamUpdate<?, ?>)
        // message).getArgument().toString();
        //          final String messageType = message.getClass().getSimpleName();
        //          arrowName = messageType + "(" + simpleName + "," + crdtId + "," + argument +
        // ")";
        //        } else {
        //          arrowName = message.toString();
        //        }

        if (message instanceof DownstreamUpdate) {
          final String simpleName =
              ((DownstreamUpdate<?, ?>) message).getCrdtType().getSimpleName();
          final String crdtId = ((DownstreamUpdate<?, ?>) message).getCrdtId();
          final String argument = ((DownstreamUpdate<?, ?>) message).getArgument().toString();
          final String messageType = message.getClass().getSimpleName();
          arrowName = messageType + "(" + simpleName + "," + crdtId + "," + argument + ")";

          final long l = System.nanoTime();

          final int sourceNodeNum = self.getPort() % 10;
          final int targetNodeNum = discoveryNode.getPort() % 10;

          diagramBuilder.addArrow(
              discoveryNode,
              message,
              arrowName,
              arrowNameColor,
              "Node" + sourceNodeNum,
              "Node" + targetNodeNum,
              l,
              0);
        }

        //      logger.debug("Node{} sent {}", () -> self.getPort() % 10, () -> l);
      }

      final ChannelFuture future = getChannel(discoveryNode).writeAndFlush(message);
      future.addListener(
          FIRE_EXCEPTION_ON_FAILURE); // Let object serialisation exceptions propagate.

      return new MyChannelFuture<>(discoveryNode, sendPromise.success(null).future());
    } catch (Exception e) {
      logger.error("Error send message", e);
      diagramBuilder.removeArror(new NodeMessagePair(discoveryNode, message));

      return new MyChannelFuture<>(discoveryNode, sendPromise.failure(e).future());
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
  public static class MyChannelFuture<Void> {

    private final DiscoveryNode discoveryNode;
    private final Future future;

    public MyChannelFuture(DiscoveryNode discoveryNode, Future future) {
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
