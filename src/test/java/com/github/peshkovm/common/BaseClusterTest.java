package com.github.peshkovm.common;

import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
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

  @AfterEach
  protected void tearDownNodes() {
    nodes.forEach(LifecycleComponent::stop);
    nodes.forEach(LifecycleComponent::close);
    nodes = Vector.empty();
    InternalClusterFactory.reset();
  }
}
