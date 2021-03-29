package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.crdt.routing.fsm.AddResourceResponse;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class AppendFailure implements Message {

  private final DiscoveryNode discoveryNode;
  private final ClientMessage clientMessage;
  private final Message resourceResponse;

  public AppendFailure(
      DiscoveryNode discoveryNode,
      ClientMessage clientMessage,
      Message resourceResponse) {
    this.discoveryNode = discoveryNode;
    this.clientMessage = clientMessage;
    this.resourceResponse = resourceResponse;
  }
}
