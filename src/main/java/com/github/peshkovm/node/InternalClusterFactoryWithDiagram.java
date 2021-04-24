package com.github.peshkovm.node;

import com.github.peshkovm.common.config.ConfigBuilder;
import com.typesafe.config.Config;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class InternalClusterFactoryWithDiagram extends InternalClusterFactory {

  private static Set<String> diagramNodesNames = HashSet.empty(); // List of diagram nodes names
  private static Set<String> diagramNodesColors = HashSet.empty(); // List of diagram nodes colors

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

  private static String diagramNodeColor() {
    final Config config = new ConfigBuilder().build();
    for (String nodeColor : config.getStringList("diagram.colors")) {
      if (!diagramNodesColors.contains(nodeColor)) {
        diagramNodesColors = diagramNodesColors.add(nodeColor);
        return nodeColor;
      }
    }
    throw new IllegalStateException(
        "Trying to create "
            + (diagramNodesColors.size() + 1)
            + " diagram nodes but only has "
            + diagramNodesColors.size()
            + " nodes colors in application.conf");
  }

  /**
   * Creates internal node on same JVM with localhost and port form application.conf.
   *
   * @return newly created InternalNode instance
   */
  public static InternalNode createInternalNode(Object... optionalBeans) {
    final String host = "127.0.0.1";
    final Config config =
        new ConfigBuilder()
            .with("transport.host", host)
            .with("transport.port", port())
            .with("diagram.node.name", diagramNodeName())
            .with("diagram.node.color", diagramNodeColor())
            .build();

    final InternalNode internalNode = new InternalNode(config, optionalBeans);

    return internalNode;
  }

  /** Resets cluster state to build new cluster from ground up */
  public static void reset() {
    InternalClusterFactory.reset();
    diagramNodesNames = HashSet.empty();
    diagramNodesColors = HashSet.empty();
  }
}
