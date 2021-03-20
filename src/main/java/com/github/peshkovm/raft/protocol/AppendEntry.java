package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class AppendEntry implements Message {

  private final DiscoveryNode discoveryNode;
  private final LogEntry entry;

  public AppendEntry(DiscoveryNode discoveryNode, LogEntry entry) {
    this.discoveryNode = discoveryNode;
    this.entry = entry;
  }
}
