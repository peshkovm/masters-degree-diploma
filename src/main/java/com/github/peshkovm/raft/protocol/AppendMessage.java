package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class AppendMessage implements Message {

  private final DiscoveryNode discoveryNode;
  private final ClientMessage message;

  public AppendMessage(DiscoveryNode discoveryNode, ClientMessage message) {
    this.discoveryNode = discoveryNode;
    this.message = message;
  }
}
