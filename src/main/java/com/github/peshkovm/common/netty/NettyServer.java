package com.github.peshkovm.common.netty;

import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Abstract server bootstrapping class.
 */
public abstract class NettyServer extends AbstractLifecycleComponent {

  protected final String host;
  protected final int port;
  protected ServerBootstrap bootstrap;
  protected NettyProvider provider;

  /**
   * Constructs a new instance.
   *
   * @param provider provides ServerSocketChannel, SocketChannel and EventLoopGroups
   * @param host server host
   * @param port serer port
   */
  protected NettyServer(NettyProvider provider, String host, int port) {
    logger.info("Initializing...");
    this.host = host;
    this.port = port;
    this.provider = provider;
    logger.info("Initialized");
  }

  /**
   * Bootstraps server.
   */
  @Override
  protected void doStart() {
    try {
      bootstrap = new ServerBootstrap();
      bootstrap
          .group(provider.getParentEventLoopGroup(), provider.getChildEventLoopGroup())
          .channel(provider.getServerSocketChannel())
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(channelInitializer());

      bootstrap.bind(host, port).sync();
    } catch (InterruptedException e) {
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

  /**
   * Does nothing
   */
  @Override
  protected void doStop() {
  }

  /**
   * Shutdowns Netty's components.
   */
  @Override
  protected void doClose() {
    bootstrap = null;
  }
}
