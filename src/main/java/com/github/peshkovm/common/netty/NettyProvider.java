package com.github.peshkovm.common.netty;

import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.typesafe.config.Config;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Provider class of Netty's components for a server and client bootstrapping. */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Getter
public class NettyProvider extends AbstractLifecycleComponent {

  private final Class<? extends ServerSocketChannel> serverSocketChannel;
  private final Class<? extends SocketChannel> clientSocketChannel;
  private final EventLoopGroup parentEventLoopGroup;
  private final EventLoopGroup childEventLoopGroup;
  private final EventExecutorGroup executor;

  /**
   * Creates {@link ServerSocketChannel}, {@link SocketChannel} and parent (acceptor) and child
   * (client) {@link EventLoopGroup}{@code s}.
   *
   * <p>SocketChannels and EventLoopGroups can be either Epoll, KQueue or NIO depending on
   * underneath OS. Order in which choice is made: Epoll->Kqueue->NIO.
   *
   * @param config defines configuration parameters
   */
  @Autowired
  public NettyProvider(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") Config config) {
    logger.debug("Initializing...");
    final int numOfParentThreads = config.getInt("netty.threads.parent");
    final int numOfChildThreads = config.getInt("netty.threads.child");

    if (Epoll.isAvailable()) {
      logger.info("Using epoll");
      serverSocketChannel = EpollServerSocketChannel.class;
      clientSocketChannel = EpollSocketChannel.class;
      parentEventLoopGroup = new EpollEventLoopGroup(numOfParentThreads);
      childEventLoopGroup = new EpollEventLoopGroup(numOfChildThreads);
    } else if (KQueue.isAvailable()) {
      logger.info("Using kqueue");
      serverSocketChannel = KQueueServerSocketChannel.class;
      clientSocketChannel = KQueueSocketChannel.class;
      parentEventLoopGroup = new KQueueEventLoopGroup(numOfParentThreads);
      childEventLoopGroup = new KQueueEventLoopGroup(numOfChildThreads);
    } else {
      logger.info("Using nio");
      serverSocketChannel = NioServerSocketChannel.class;
      clientSocketChannel = NioSocketChannel.class;
      parentEventLoopGroup = new NioEventLoopGroup(numOfParentThreads);
      childEventLoopGroup = new NioEventLoopGroup(numOfChildThreads);
    }
    executor = new DefaultEventExecutorGroup(1);
    logger.debug("Initialized");
  }

  /** Does nothing. */
  @Override
  protected void doStart() {}

  /** Does nothing. */
  @Override
  protected void doStop() {}

  /** Shutdowns Netty's components. */
  @Override
  protected void doClose() {
    try {
      childEventLoopGroup.shutdownGracefully().sync();
      parentEventLoopGroup.shutdownGracefully().sync();
      executor.shutdownGracefully().sync();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      e.printStackTrace();
    }
  }
}
