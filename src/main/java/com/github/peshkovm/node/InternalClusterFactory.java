package com.github.peshkovm.node;

import com.github.peshkovm.common.config.ConfigBuilder;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Factory class to creates {@link InternalNode} instances. */
public final class InternalClusterFactory {

  private static final Set<Integer> ports = Sets.newHashSet(); // List of cluster nodes ports

  private InternalClusterFactory() {}

  private static int nextPort() {
    for (; ; ) {
      int port = 8800 + ThreadLocalRandom.current().nextInt(100);
      if (!ports.contains(port)) {
        ports.add(port);
        return port;
      }
    }
  }

  /**
   * Creates internal node on same JVM with random port.
   *
   * @return newly created InternalNode instance
   */
  public static InternalNode createInternalNode() {
    final int port = nextPort();
    final Config config = new ConfigBuilder().with("transport.port", port).build();

    final InternalNode node = new InternalNode(config);

    return node;
  }

  /** Resets cluster state to build new cluster from ground up */
  public static void reset() {
    ports.clear();
  }
}
