package com.github.peshkovm.node;

import com.github.peshkovm.common.config.ConfigBuilder;
import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

/** Factory class to creates {@link InternalNode} instances. */
public final class InternalClusterFactory {

  private static Set<Integer> ports = HashSet.empty(); // List of cluster nodes ports
  private static Set<String> diagramNodesNames = HashSet.empty(); // List of diagram nodes names

  private InternalClusterFactory() {}

  private static int port() {
    final Config config = new ConfigBuilder().build();
    for (String nodeAddress : config.getStringList("raft.discovery.internal_nodes")) {
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

  private static String diagramNodeName() {
    final Config config = new ConfigBuilder().build();
    for (String nodeName : config.getStringList("diagram.nodes")) {
      if (!diagramNodesNames.contains(nodeName)) {
        diagramNodesNames = diagramNodesNames.add(nodeName);
        return nodeName;
      }
    }
    throw new IllegalStateException(
        "Trying to create "
            + (diagramNodesNames.size() + 1)
            + " diagram nodes but only has "
            + diagramNodesNames.size()
            + " diagram nodes in application.conf");
  }

  /**
   * Creates internal node on same JVM with localhost and port form application.conf.
   *
   * @return newly created InternalNode instance
   */
  public static InternalNode createInternalNode(Object... optionalBeans) {
    final String host = "127.0.0.1";
    final int port = port();
    final String diagramNodeName = diagramNodeName();
    final Config config =
        new ConfigBuilder()
            .with("transport.host", host)
            .with("transport.port", port)
            .with("diagram.node.name", diagramNodeName)
            .build();

    final InternalNode internalNode = new InternalNode(config, optionalBeans);

    return internalNode;
  }

  /** Resets cluster state to build new cluster from ground up */
  public static void reset() {
    ports = HashSet.empty();
  }
}
