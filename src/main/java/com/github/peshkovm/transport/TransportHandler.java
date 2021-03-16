package com.github.peshkovm.transport;

import com.github.peshkovm.common.codec.Message;
import io.netty.channel.ChannelHandlerContext;

/**
 * Represents an operation that handles a message and returns no result.
 *
 * @param <T> the type of the message to the operation
 */
@FunctionalInterface
public interface TransportHandler<T extends Message> {

  /**
   * Handles message. Will be called in {@link io.netty.channel.SimpleChannelInboundHandler#channelRead0(ChannelHandlerContext,
   * Object) channelRead0(ChannelHandlerContext, Object) of both server and client.}
   *
   * @param message the message
   */
  void handle(T message);
}
