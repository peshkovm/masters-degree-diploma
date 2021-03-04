package com.github.peshkovm.common;

import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.node.InternalNodeFactory;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.jupiter.api.AfterEach;

/**
 * Provides methods for cluster testing.
 */
public class BaseClusterTest {

  protected List<InternalNode> nodes = Lists.newArrayList();

  /** Creates leader node on local host with random port */
  protected final void createAndStartLeader() {
    final InternalNode node = InternalNodeFactory.createLeaderNode();
    node.start();
    nodes.add(node);
  }

  /** Creates follower node on local host with random port */
  protected final void createAndStartFollower() {
    final InternalNode node = InternalNodeFactory.createFollowerNode();
    node.start();
    nodes.add(node);
  }

  @AfterEach
  protected void tearDownNodes() {
    nodes.forEach(LifecycleComponent::stop);
    nodes.forEach(LifecycleComponent::close);
    nodes = Lists.newArrayList();
    InternalNodeFactory.reset();
  }
}
