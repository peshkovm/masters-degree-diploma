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

  /** Creates and starts follower node on sme JVM with random port. */
  protected final void createAndStartInternalNode() {
    final InternalNode node = InternalClusterFactory.createInternalNode();
    nodes.add(node);

    node.start();
  }

  @AfterEach
  protected void tearDownNodes() {
    nodes.forEach(LifecycleComponent::stop);
    nodes.forEach(LifecycleComponent::close);
    nodes = Lists.newArrayList();
    InternalClusterFactory.reset();
  }

  protected int getLeaderIndex() {
    return nodes.indexOf(
        nodes.stream()
            .filter(node -> node.getConfig().getBoolean("transport.is_leader"))
            .findFirst()
            .get());
  }
}
