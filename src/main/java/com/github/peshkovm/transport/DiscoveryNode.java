package com.github.peshkovm.transport;

import com.github.peshkovm.common.codec.Message;
import com.google.common.base.Preconditions;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/** Represents node's host and port. */
@Data
public class DiscoveryNode implements Message, Comparable<DiscoveryNode> {

  private final String host;
  private final int port;

  public DiscoveryNode(String host, int port) {
    Preconditions.checkNotNull(host);
    this.host = host;
    this.port = port;
  }

  @Override
  public int compareTo(@NotNull DiscoveryNode that) {
    return Integer.compare(this.port, that.port);
  }
}
