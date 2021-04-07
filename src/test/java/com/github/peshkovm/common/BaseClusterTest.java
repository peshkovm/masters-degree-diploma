package com.github.peshkovm.common;

import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.netty.NettyTransportService;
import io.vavr.collection.Vector;
import org.junit.jupiter.api.AfterEach;

/**
 * Provides methods for cluster testing.
 */
public class BaseClusterTest extends BaseTest {

  protected Vector<InternalNode> nodes = Vector.empty();

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
                .get((i + 1 + j) % nodes.size())
                .getBeanFactory()
                .getBean(ClusterDiscovery.class)
                .getSelf());
      }
    }
  }

  @AfterEach
  protected void tearDownNodes() {
    nodes.forEach(LifecycleComponent::stop);
    nodes.forEach(LifecycleComponent::close);
    nodes = Vector.empty();
    InternalClusterFactory.reset();
  }
}
