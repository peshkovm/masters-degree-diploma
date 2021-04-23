package com.github.peshkovm.main.common;

import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.netty.NettyTransportService;
import io.vavr.collection.Vector;

/** Provides methods for cluster testing. */
public class TestUtils extends BaseTestUtils {

  protected Vector<InternalNode> nodes = Vector.empty();
  private DiagramFactorySingleton diagramHelper;

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

      transportService.connectToNode(
          nodes
              .get((i + 1) % nodes.size())
              .getBeanFactory()
              .getBean(ClusterDiscovery.class)
              .getSelf());
      transportService.connectToNode(
          nodes
              .get((i + 2) % nodes.size())
              .getBeanFactory()
              .getBean(ClusterDiscovery.class)
              .getSelf());
    }
  }

  protected void tearDownNodes() {
    if (diagramHelper.isDiagramIsActive()) {
      diagramHelper.buildDiagram();
    }

    nodes.forEach(LifecycleComponent::stop);
    nodes.forEach(LifecycleComponent::close);
    nodes = Vector.empty();
    InternalClusterFactory.reset();
  }

  protected void setUpDiagram(String diagramName, int nodeHeight, String outputPath) {
    diagramHelper =
        nodes.map(node -> node.getBeanFactory().getBean(DiagramFactorySingleton.class)).get(0);

    if (diagramHelper.isDiagramIsActive()) {
      diagramHelper.createDiagram(diagramName, nodeHeight);
      diagramHelper.setOutputPath(outputPath);
      diagramHelper.addNode(nodes.get(0), DrawIOColor.ORANGE);
      diagramHelper.addNode(nodes.get(1), DrawIOColor.BLUE);
      diagramHelper.addNode(nodes.get(2), DrawIOColor.GREEN);
    }
  }
}
