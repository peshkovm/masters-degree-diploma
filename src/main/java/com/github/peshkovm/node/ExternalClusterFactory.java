package com.github.peshkovm.node;

import com.github.peshkovm.common.config.ConfigBuilder;
import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ExternalClusterFactory {

  private static volatile InternalNode internalNode;

  private ExternalClusterFactory() {
  }

  private static void verifyHostAndPort(String host, int port) {
    final Config config = new ConfigBuilder().build();

    final Set<HostAndPort> hostAndPorts =
        new HashSet<>(
            config.getStringList("raft.discovery.external_nodes").stream()
                .map(HostAndPort::fromString)
                .collect(Collectors.toList()));

    if (!hostAndPorts.contains(HostAndPort.fromParts(host, port))) {
      throw new IllegalArgumentException(
          "application.conf does mot contain address: " + host + ":" + port);
    }
  }

  /**
   * Returns singleton internal node on same JVM with host and port from application.conf.
   *
   * @return newly created InternalNode instance
   */
  public static InternalNode getInternalNode(String host, int port) {
    if (internalNode != null) {
      return internalNode;
    }
    synchronized (ExternalClusterFactory.class) {
      if (internalNode == null) {
        verifyHostAndPort(host, port);
        final Config config =
            new ConfigBuilder().with("transport.host", host).with("transport.port", port).build();

        internalNode = new ExternalNode(config);
      }

      return internalNode;
    }
  }
}
