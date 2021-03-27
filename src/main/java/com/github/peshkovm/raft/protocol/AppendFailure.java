package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class AppendFailure implements Message {

  private final DiscoveryNode discoveryNode;
  private final AppendMessage message;

  public AppendFailure(DiscoveryNode discoveryNode, AppendMessage message) {
    this.discoveryNode = discoveryNode;
    this.message = message;
  }
}
