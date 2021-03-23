package com.github.peshkovm.cluster;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InternalClusterFactoryTest extends BaseClusterTest {

  @Test
  @DisplayName("Should create new node every time")
  void shouldCreateNewNodeEveryTime() {
    final ArrayList<InternalNode> nodes = new ArrayList<>();

    nodes.add(InternalClusterFactory.createInternalNode());
    nodes.add(InternalClusterFactory.createInternalNode());
    nodes.add(InternalClusterFactory.createInternalNode());

    Assertions.assertEquals(nodes.stream().distinct().count(), 3);

    nodes.forEach(LifecycleComponent::stop);
    nodes.forEach(LifecycleComponent::close);
    InternalClusterFactory.reset();
  }
}
