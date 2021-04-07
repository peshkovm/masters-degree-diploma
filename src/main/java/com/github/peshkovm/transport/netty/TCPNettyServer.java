package com.github.peshkovm.transport.netty;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.diagram.DiagramBuilderSingleton;
import com.github.peshkovm.common.diagram.MxCellPojo;
import com.github.peshkovm.common.diagram.NodeMessagePair;
import com.github.peshkovm.common.netty.NettyProvider;
import com.github.peshkovm.common.netty.NettyServer;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportController;
import com.github.peshkovm.transport.TransportServer;
import com.typesafe.config.Config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link NettyServer}.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TCPNettyServer extends NettyServer implements TransportServer {

  private final TransportController transportController;
  private final DiagramBuilderSingleton diagramBuilder;
  private final DiscoveryNode self;

  /**
   * Constructs a new instance.
   *
   * @param config defines parameters host and port
   * @param provider provides ServerSocketChannel, SocketChannel and EventLoopGroups
   * @param transportController transport controller to dispatch messages
   */
  @Autowired
  public TCPNettyServer(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") Config config,
      NettyProvider provider,
      TransportController transportController,
      DiagramBuilderSingleton diagramBuilder,
      ClusterDiscovery clusterDiscovery) {
    super(
        new DiscoveryNode(config.getString("transport.host"), config.getInt("transport.port")),
        provider);
    this.transportController = transportController;
    this.diagramBuilder = diagramBuilder;
    self = clusterDiscovery.getSelf();
  }

  @Override
  protected ChannelInitializer<Channel> channelInitializer() {
    return new ServerChannelInitializer();
  }

  private class ServerChannelInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();
      pipeline.addLast(
          new LoggingHandler(
              LoggingHandler.class.getName() + "." + this.getClass().getSimpleName() + ".Channel"));
      pipeline.addLast(new ObjectEncoder());
      pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
      pipeline.addLast(provider.getExecutor(), new TransportServerHandler());
    }
  }

  private class TransportServerHandler extends SimpleChannelInboundHandler<Message> {

    public TransportServerHandler() {
      super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
      final long l;
      final MxCellPojo arrow =
          diagramBuilder.getMessageArrowMap().get(new NodeMessagePair(self, message));
      if (arrow != null) {
        l = System.nanoTime();
        arrow.getMxGeometry().getMxPoints().get(1).setY(l);
        //        logger.debug("Node{} received {}", () -> self.getPort() % 10, () -> l);
      }

      transportController.dispatch(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      logger.error("Unexpected channel error, closing channel...", cause);
      ctx.close();
    }
  }
}
