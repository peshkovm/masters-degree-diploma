package com.github.peshkovm.common.netty;

import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.logging.LoggingHandler;

/**
 * Abstract server bootstrapping class.
 */
public abstract class NettyServer extends AbstractLifecycleComponent implements TransportServer {

  protected DiscoveryNode discoveryNode;
  protected ServerBootstrap bootstrap;
  protected NettyProvider provider;
  /**
   * Constructs a new instance.
   *
   * @param provider provides ServerSocketChannel, SocketChannel and EventLoopGroups
   * @param discoveryNode node's bind address
   */
  protected NettyServer(DiscoveryNode discoveryNode, NettyProvider provider) {
    logger.debug("Initializing...");
    this.discoveryNode = discoveryNode;
    this.provider = provider;
    logger.debug("Initialized");
  }

  /** Binds server to host and port, assigned in constructor */
  @Override
  protected void doStart() {
    try {
      bootstrap = new ServerBootstrap();
      bootstrap
          .group(provider.getParentEventLoopGroup(), provider.getChildEventLoopGroup())
          .channel(provider.getServerSocketChannel())
          .handler(
              new LoggingHandler(
                  LoggingHandler.class.getName()
                      + "."
                      + this.getClass().getSimpleName()
                      + ".ServerChannel"))
          .childHandler(channelInitializer());

      bootstrap.bind(discoveryNode.getHost(), discoveryNode.getPort()).sync();
      logger.info(
          "Server listening on {}:{}",
          () -> discoveryNode.getHost(),
          () -> discoveryNode.getPort());
    } catch (InterruptedException e) {
      logger.error("Error bind to {}", discoveryNode, e);
      Thread.currentThread().interrupt();
      e.printStackTrace();
    }
  }

  /**
   * Returns {@link ChannelInitializer} instance.
   *
   * @return ChannelInitializer instance.
   */
  protected abstract ChannelInitializer<Channel> channelInitializer();

  @Override
  public DiscoveryNode localNode() {
    return discoveryNode;
  }

  /** Does nothing */
  @Override
  protected void doStop() {}

  /** Shutdowns Netty's components. */
  @Override
  protected void doClose() {
    bootstrap = null;
  }
}
