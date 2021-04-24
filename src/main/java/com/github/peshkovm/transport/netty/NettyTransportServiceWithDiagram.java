package com.github.peshkovm.transport.netty;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.netty.NettyProvider;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.discovery.ClusterDiagramNodeDiscovery;
import com.github.peshkovm.diagram.netty.DiagramArrowCodec;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportController;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("diagram")
public class NettyTransportServiceWithDiagram extends NettyTransportService {

  private final DiagramArrowCodec diagramArrowCodec;

  /**
   * Constructs a new instance using provider for bootstrapping.
   *
   * @param provider provides SocketChannel and EventLoopGroup
   * @param transportController transport controller to dispatch messages
   */
  @Autowired
  public NettyTransportServiceWithDiagram(
      NettyProvider provider,
      TransportController transportController,
      DiagramFactorySingleton diagramFactorySingleton,
      ClusterDiagramNodeDiscovery clusterDiagramNodeDiscovery) {
    super(provider, transportController);
    diagramArrowCodec = new DiagramArrowCodec(diagramFactorySingleton, clusterDiagramNodeDiscovery);
  }

  @Override
  protected ChannelInitializer<Channel> channelInitializer() {
    return new ClientChannelInitializerWithDiagram();
  }

  private class ClientChannelInitializerWithDiagram extends ClientChannelInitializer {

    @Override
    protected void initChannel(Channel ch) throws Exception {
      super.initChannel(ch);
      final ChannelPipeline pipeline = ch.pipeline();
      pipeline.addBefore(
          TransportClientHandler.class.getSimpleName(),
          DiagramArrowCodec.class.getSimpleName(),
          diagramArrowCodec);
    }
  }

  @Override
  public DiscoveryFuture send(DiscoveryNode discoveryNode, Message message) {
    final DiscoveryFuture discoveryFuture = super.send(discoveryNode, message);
    if (discoveryFuture.getFuture().isFailure()) {
      diagramArrowCodec.onException(
          discoveryFuture.getFuture().getCause().get(), discoveryNode, message);
    }
    return discoveryFuture;
  }
}
