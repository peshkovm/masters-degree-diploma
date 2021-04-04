package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class ClientMessage implements Message {

  private final DiscoveryNode discoveryNode;
  private final ClientCommand message;

  public ClientMessage(DiscoveryNode discoveryNode, ClientCommand message) {
    this.discoveryNode = discoveryNode;
    this.message = message;
  }
}
