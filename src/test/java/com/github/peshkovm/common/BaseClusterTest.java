package com.github.peshkovm.common;

import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.jupiter.api.AfterEach;

/**
 * Provides methods for cluster testing.
 */
public class BaseClusterTest extends BaseTest {

  protected List<InternalNode> nodes = Lists.newArrayList();

  /**
   * Creates and starts leader node on same JVM with random port.
   */
  protected final void createAndStartLeader() {
    final InternalNode node = InternalClusterFactory.createLeaderNode();
    nodes.add(node);
    node.start();
  }

  /** Creates and starts follower node on sme JVM with random port. */
  protected final void createAndStartFollower() {
    final InternalNode node = InternalClusterFactory.createFollowerNode();
    node.start();
    nodes.add(node);
  }

  @AfterEach
  protected void tearDownNodes() {
    nodes.forEach(LifecycleComponent::stop);
    nodes.forEach(LifecycleComponent::close);
    nodes = Lists.newArrayList();
    InternalClusterFactory.reset();
  }
}
