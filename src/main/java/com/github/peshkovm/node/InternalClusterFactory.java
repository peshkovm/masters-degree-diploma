package com.github.peshkovm.node;

import com.github.peshkovm.common.config.ConfigBuilder;
import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

/**
 * Factory class to creates {@link InternalNode} instances.
 */
public final class InternalClusterFactory {

  private static Set<Integer> ports = HashSet.empty(); // List of cluster nodes ports

  private InternalClusterFactory() {}

  private static int port() {
    final Config config = new ConfigBuilder().build();
    for (String nodeAddress : config.getStringList("raft.discovery.nodes")) {
      final HostAndPort hostAndPort = HostAndPort.fromString(nodeAddress);
      final int port = hostAndPort.getPort();

      if (!ports.contains(port)) {
        ports = ports.add(port);
        return port;
      }
    }
    throw new IllegalStateException(
        "Trying to create "
            + (ports.size() + 1)
            + " nodes but only has "
            + ports.size()
            + " ports in application.conf");
  }

  /**
   * Creates internal node on same JVM with random port.
   *
   * @return newly created InternalNode instance
   */
  public static InternalNode createInternalNode() {
    final int port = port();
    final Config config =
        new ConfigBuilder()
            .with("transport.port", port)
            // .with("raft.discovery.nodes.0", "127.0.0.1:" + port)
            .build();

    final InternalNode node = new InternalNode(config);

    return node;
  }

  /** Resets cluster state to build new cluster from ground up */
  public static void reset() {
    ports = HashSet.empty();
  }
}
