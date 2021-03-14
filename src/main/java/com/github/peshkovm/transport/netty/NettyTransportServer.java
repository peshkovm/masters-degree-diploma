package com.github.peshkovm.transport.netty;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.netty.NettyProvider;
import com.github.peshkovm.common.netty.NettyServer;
import com.typesafe.config.Config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link NettyServer}.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NettyTransportServer extends NettyServer implements TransportServer {

  /**
   * Constructs a new instance.
   *
   * @param config defines parameters for bootstrapping, host and port
   * @param provider provides ServerSocketChannel, SocketChannel and EventLoopGroups
   */
  @Autowired
  public NettyTransportServer(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") Config config,
      NettyProvider provider) {
    super(provider, config.getString("transport.host"), config.getInt("transport.port"));
  }

  @Override
  protected ChannelInitializer<Channel> channelInitializer() {
    return new TransportServerInitializer();
  }

  @ChannelHandler.Sharable
  private class TransportServerInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();
      pipeline.addLast(new TransportServerHandler());
    }
  }

  private class TransportServerHandler extends SimpleChannelInboundHandler<Message> {

    public TransportServerHandler() {
      super(true);
    }

    @Override
    public boolean isSharable() {
      return super.isSharable();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      logger.error("Unexpected channel error, close channel", cause);
      ctx.close();
    }
  }
}
