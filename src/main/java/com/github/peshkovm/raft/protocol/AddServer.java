package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class AddServer implements Message {

  private final DiscoveryNode discoveryNode;

  public AddServer(DiscoveryNode discoveryNode) {
    this.discoveryNode = discoveryNode;
  }
}
