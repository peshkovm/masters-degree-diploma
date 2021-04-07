package com.github.peshkovm.common.diagram;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class NodeMessagePair {

  private final DiscoveryNode discoveryNode;
  private final Message message;
}
