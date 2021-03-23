package com.github.peshkovm.node;

import com.github.peshkovm.common.config.ConfigBuilder;
import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;
import java.util.Set;

/**
 * Factory class to creates {@link InternalNode} instances.
 */
public final class InternalClusterFactory {

  private static final Set<Integer> ports = Sets.newHashSet(); // List of cluster nodes ports

  private InternalClusterFactory() {}

  private static int port() {
    //    for (; ; ) {
    //      int port = 8800 + ThreadLocalRandom.current().nextInt(100);
    //      if (!ports.contains(port)) {
    //        ports.add(port);
    //        return port;
    //      }
    //    }

    final Config config = new ConfigBuilder().build();
    for (String nodeAddress : config.getStringList("raft.discovery.nodes")) {
      final HostAndPort hostAndPort = HostAndPort.fromString(nodeAddress);
      final int port = hostAndPort.getPort();

      if (!ports.contains(port)) {
        ports.add(port);
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
    ports.clear();
  }
}
