package com.github.peshkovm.main.common;

import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.MessageType;
import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.netty.NettyTransportService;
import io.vavr.collection.Vector;

/** Provides methods for cluster testing. */
public class TestUtils extends BaseTestUtils {

  protected Vector<InternalNode> nodes = Vector.empty();
  private DiagramFactorySingleton diagramFactorySingleton;

  /** Creates and starts follower node on sme JVM with random port. */
  protected final void createAndStartInternalNode() {
    final InternalNode node = InternalClusterFactory.createInternalNode();
    nodes = nodes.append(node);

    node.start();
  }

  protected void connectAllNodes() {
    for (int i = 0; i < nodes.size(); i++) {
      final InternalNode sourceNode = nodes.get(i);

      final NettyTransportService transportService =
          sourceNode.getBeanFactory().getBean(NettyTransportService.class);

      for (int j = 0; j < nodes.size() - 1; j++) {
        transportService.connectToNode(
            nodes
                .get((i + j + 1) % nodes.size())
                .getBeanFactory()
                .getBean(ClusterDiscovery.class)
                .getSelf());
      }
    }
  }

  protected void tearDownNodes() {
    if (diagramFactorySingleton != null && diagramFactorySingleton.isDiagramActive()) {
      diagramFactorySingleton.buildDiagram();
    }

    nodes.forEach(LifecycleComponent::stop);
    nodes.forEach(LifecycleComponent::close);
    nodes = Vector.empty();
    InternalClusterFactory.reset();
  }

  protected void setUpDiagram(
      String diagramName,
      int nodeHeight,
      String outputPath,
      boolean isDrawOnError,
      MessageType... msgsToSkip) {
    diagramFactorySingleton =
        nodes.map(node -> node.getBeanFactory().getBean(DiagramFactorySingleton.class)).get(0);

    if (diagramFactorySingleton.isDiagramActive()) {
      diagramFactorySingleton.createDiagram(diagramName, nodeHeight, isDrawOnError, msgsToSkip);
      diagramFactorySingleton.setOutputPath(outputPath);
      for (int i = 0; i < nodes.size(); i++) {
        final InternalNode internalNode = nodes.get(i);
        diagramFactorySingleton.addNode(internalNode, DrawIOColor.values()[i]);
      }
    }
  }

  protected void checkNumberOfCreatedNodes() {
    final ClusterDiscovery clusterDiscovery =
        nodes.get(0).getBeanFactory().getBean(ClusterDiscovery.class);

    if (nodes.size() != clusterDiscovery.getDiscoveryNodes().size())
      throw new IllegalStateException(
          "Created "
              + nodes.size()
              + " nodes, but has "
              + clusterDiscovery.getDiscoveryNodes().size()
              + " nodes in application.conf");
  }
}
