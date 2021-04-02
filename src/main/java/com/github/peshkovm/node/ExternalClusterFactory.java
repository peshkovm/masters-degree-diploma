package com.github.peshkovm.node;

import com.github.peshkovm.common.config.ConfigBuilder;
import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;

public class ExternalClusterFactory {

  private static volatile InternalNode internalNode;

  private ExternalClusterFactory() {
  }

  private static String host() {
    final Config config = new ConfigBuilder().build();

    final String sourceNodeAddress = config.getStringList("raft.discovery.external_nodes").get(0);
    final HostAndPort hostAndPort = HostAndPort.fromString(sourceNodeAddress);
    final String host = hostAndPort.getHost();

    return host;
  }

  private static int port() {
    final Config config = new ConfigBuilder().build();

    final String sourceNodeAddress = config.getStringList("raft.discovery.external_nodes").get(0);
    final HostAndPort hostAndPort = HostAndPort.fromString(sourceNodeAddress);
    final int port = hostAndPort.getPort();

    return port;
  }

  /**
   * Returns singleton internal node on same JVM with host and port from application.conf.
   *
   * @return newly created InternalNode instance
   */
  public static InternalNode getInternalNode() {
    if (internalNode != null) {
      return internalNode;
    }
    synchronized (ExternalClusterFactory.class) {
      if (internalNode == null) {
        final String host = host();
        final int port = port();
        final Config config =
            new ConfigBuilder().with("transport.host", host).with("transport.port", port).build();

        internalNode = new ExternalNode(config);
      }

      return internalNode;
    }
  }
}
