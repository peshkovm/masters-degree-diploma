package com.github.peshkovm.cluster;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import io.vavr.collection.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InternalClusterFactoryTest extends BaseClusterTest {

  @Test
  @DisplayName("Should create new node every time")
  void shouldCreateNewNodeEveryTime() {
    List<InternalNode> nodes = List.empty();

    nodes = nodes.append(InternalClusterFactory.createInternalNode());
    nodes = nodes.append(InternalClusterFactory.createInternalNode());
    nodes = nodes.append(InternalClusterFactory.createInternalNode());

    Assertions.assertEquals(nodes.distinct().size(), 3);

    InternalClusterFactory.reset();
  }
}
