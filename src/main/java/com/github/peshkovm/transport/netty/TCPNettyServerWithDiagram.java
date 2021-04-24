package com.github.peshkovm.transport.netty;

import com.github.peshkovm.common.netty.NettyProvider;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.discovery.ClusterDiagramNodeDiscovery;
import com.github.peshkovm.diagram.netty.DiagramArrowCodec;
import com.github.peshkovm.transport.TransportController;
import com.typesafe.config.Config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("diagram")
public class TCPNettyServerWithDiagram extends TCPNettyServer {

  private final DiagramArrowCodec diagramArrowCodec;

  /**
   * Constructs a new instance.
   *
   * @param config defines parameters host and port
   * @param provider provides ServerSocketChannel, SocketChannel and EventLoopGroups
   * @param transportController transport controller to dispatch messages
   */
  public TCPNettyServerWithDiagram(
      Config config,
      NettyProvider provider,
      TransportController transportController,
      DiagramFactorySingleton diagramFactorySingleton,
      ClusterDiagramNodeDiscovery clusterDiagramNodeDiscovery) {
    super(config, provider, transportController);
    diagramArrowCodec = new DiagramArrowCodec(diagramFactorySingleton, clusterDiagramNodeDiscovery);
  }

  @Override
  protected ChannelInitializer<Channel> channelInitializer() {
    return new ServerChannelInitializerWithDiagram();
  }

  private class ServerChannelInitializerWithDiagram extends ServerChannelInitializer {

    @Override
    protected void initChannel(Channel ch) throws Exception {
      super.initChannel(ch);
      ChannelPipeline pipeline = ch.pipeline();
      pipeline.addBefore(
          TransportServerHandler.class.getSimpleName(),
          DiagramArrowCodec.class.getSimpleName(),
          diagramArrowCodec);
    }
  }
}
