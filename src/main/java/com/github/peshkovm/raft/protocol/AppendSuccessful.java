package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class AppendSuccessful implements Message {

  private final DiscoveryNode discoveryNode;
  private final ClientMessage clientMessage;

  public AppendSuccessful(DiscoveryNode discoveryNode, ClientMessage clientMessage) {
    this.discoveryNode = discoveryNode;
    this.clientMessage = clientMessage;
  }
}
