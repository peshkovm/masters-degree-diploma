package com.github.peshkovm.transport.netty;

import com.github.peshkovm.common.codec.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EncoderDecoderTest {

  @Test
  @DisplayName("Should decode and encode message")
  void shouldDecodeAndEncodeMessage() throws InterruptedException {
    final int msgValue = 55555;
    final IntegerMessage msg = new IntegerMessage(msgValue);
    final EmbeddedChannel channel =
        new EmbeddedChannel(
            new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)));

    Assertions.assertTrue(channel.writeOutbound(msg));
    final Object outboundMsg = channel.readOutbound();
    Assertions.assertTrue(outboundMsg instanceof ByteBuf);

    Assertions.assertTrue(channel.writeInbound(outboundMsg));
    final Object inboundMsg = channel.readInbound();
    Assertions.assertTrue(inboundMsg instanceof IntegerMessage);

    Assertions.assertEquals(((IntegerMessage) inboundMsg).getValue(), msgValue);
  }

  @Data
  private static class IntegerMessage implements Message {

    private final Integer value;

    public IntegerMessage(Integer value) {
      this.value = value;
    }
  }
}
