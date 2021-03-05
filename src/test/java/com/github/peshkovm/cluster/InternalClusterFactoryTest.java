package com.github.peshkovm.cluster;

import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InternalClusterFactoryTest {
  @Test
  @DisplayName("Should create new node every time")
  void shouldCreateNewNodeEveryTime() {
    final ArrayList<InternalNode> nodes = new ArrayList<>();

    nodes.add(InternalClusterFactory.createLeaderNode());
    nodes.add(InternalClusterFactory.createFollowerNode());
    nodes.add(InternalClusterFactory.createFollowerNode());
    nodes.add(InternalClusterFactory.createFollowerNode());

    Assertions.assertEquals(nodes.stream().distinct().count(), 4);
  }
}
