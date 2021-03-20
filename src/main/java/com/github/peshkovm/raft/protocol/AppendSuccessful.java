package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class AppendSuccessful implements Message {

  private final DiscoveryNode discoveryNode;
  private final long session;

  public AppendSuccessful(DiscoveryNode discoveryNode, long session) {
    this.discoveryNode = discoveryNode;
    this.session = session;
  }
}
