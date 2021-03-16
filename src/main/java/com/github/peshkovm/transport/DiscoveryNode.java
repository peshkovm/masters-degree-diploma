package com.github.peshkovm.transport;

import com.github.peshkovm.common.codec.Message;
import com.google.common.base.Preconditions;
import lombok.Data;

/**
 * Represents node's host and port.
 */
@Data
public class DiscoveryNode implements Message {

  private final String host;
  private final int port;

  public DiscoveryNode(String host, int port) {
    Preconditions.checkNotNull(host);
    this.host = host;
    this.port = port;
  }
}
