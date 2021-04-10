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
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.routing.fsm.AddResource;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.raft.protocol.ClientMessage;
import com.github.peshkovm.raft.protocol.ClientMessageSuccessful;
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
import io.vavr.control.Option;
import java.io.Serializable;
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

      addArrowToDiagram(discoveryNode, message);

      final ChannelFuture future = getChannel(discoveryNode).writeAndFlush(message);

      future.addListener(
          FIRE_EXCEPTION_ON_FAILURE); // Let object serialisation exceptions propagate.

      return new MyChannelFuture<>(discoveryNode, sendPromise.success(null).future());
    } catch (Exception e) {
      changeArrowToError(discoveryNode, message);
      logger.error("Error send message", e);

      return new MyChannelFuture<>(discoveryNode, sendPromise.failure(e).future());
    }
  }

  private void addArrowToDiagram(DiscoveryNode discoveryNode, Message message) {
    if (diagramBuilder.isActive()) {
      String arrowName = getArrowName(message);
      String arrowColor = getArrowColor(message);
      String startArrow = getStartArrow(message);
      String endArrow = getEndArrow(message);

      if (message instanceof DownstreamUpdate) {
        final int sourceNodeNum = self.getPort() % 10;
        final int targetNodeNum = discoveryNode.getPort() % 10;

        final long l = System.nanoTime();

        logger.warn("Service l = {}", () -> l);

        diagramBuilder.addArrow(
            discoveryNode,
            message,
            arrowName,
            arrowColor,
            startArrow,
            endArrow,
            "Node" + sourceNodeNum,
            "Node" + targetNodeNum,
            l,
            0);
      }
    }
  }

  private String getEndArrow(Message message) {
    String endArrow = "classic";

    return endArrow;
  }

  private String getStartArrow(Message message) {
    String startArrow;

    if (message instanceof ClientMessage || message instanceof ClientMessageSuccessful) {
      startArrow = "diamond";
    } else {
      startArrow = "oval";
    }

    return startArrow;
  }

  private String getArrowName(Message message) {
    String arrowName = "";

    if (message instanceof ClientMessage) {
      if (((ClientMessage) message).getMessage().getCommand() instanceof AddResource) {
        final ResourceType resourceType =
            ((AddResource) ((ClientMessage) message).getMessage().getCommand()).getResourceType();
        final String resourceId =
            ((AddResource) ((ClientMessage) message).getMessage().getCommand()).getResourceId();

        arrowName = resourceType + " " + resourceId;
      }
    } else if (message instanceof ClientMessageSuccessful) {
    } else if (message instanceof DownstreamUpdate) {
      final Option<?> atSourceResult = ((DownstreamUpdate<?, ?>) message).getAtSourceResult();
      final Serializable argument = ((DownstreamUpdate<?, ?>) message).getArgument();

      arrowName = atSourceResult + ", " + argument;
    } else {
      arrowName = message.toString();
    }

    return arrowName;
  }

  private String getArrowColor(Message message) {
    String arrowColor;

    if (message instanceof ClientMessageSuccessful) {
      if (((ClientMessageSuccessful) message).getCommandResult().isSuccessful()) {
        arrowColor = DrawIOColor.GREEN.strokeColor;
      } else {
        arrowColor = DrawIOColor.RED.strokeColor;
      }
    } else {
      arrowColor = DrawIOColor.GREY.strokeColor;
    }

    return arrowColor;
  }

  private void changeArrowToError(DiscoveryNode discoveryNode, Message message) {
    final long l = System.nanoTime();
    //      diagramBuilder.removeArrow(new NodeMessagePair(discoveryNode, message));
    final NodeMessagePair nodeMessagePair = new NodeMessagePair(discoveryNode, message);

    diagramBuilder.setEndArrow(nodeMessagePair, "cross");
    final MxCellPojo arrow =
        diagramBuilder.getMessageArrowMap().get(new NodeMessagePair(discoveryNode, message));
    arrow.getMxGeometry().getMxPoints().get(1).setY(l);
    diagramBuilder.setArrowColor(nodeMessagePair, DrawIOColor.RED.strokeColor);
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
