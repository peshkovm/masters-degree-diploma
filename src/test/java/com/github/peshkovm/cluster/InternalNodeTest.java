package com.github.peshkovm.cluster;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.transport.TransportServer;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InternalNodeTest extends BaseClusterTest {

  @Test
  @DisplayName("Should start then stop and close node")
  void shouldStartThenStopAndCloseNode() {
    createAndStartInternalNode();
    final InternalNode followerNode = nodes.get(0);

    Assertions.assertTrue(followerNode.isStarted());
    followerNode.stop();
    Assertions.assertTrue(followerNode.isStopped());
    followerNode.close();
    Assertions.assertTrue(followerNode.isClosed());
    nodes = Lists.newArrayList();
  }

  @Test
  @DisplayName("Should create all nodes on localhost")
  void shouldCreateAllNodesOnLocalhost() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    Assertions.assertEquals(
        nodes.stream()
            .map(
                internalNode ->
                    internalNode
                        .getBeanFactory()
                        .getBean(TransportServer.class)
                        .localNode()
                        .getHost())
            .distinct()
            .collect(Collectors.toList()),
        Collections.singletonList("127.0.0.1"));
  }

  @Test
  @DisplayName("Should create all nodes on different ports")
  void shouldCreateAllNodesOnDifferentPorts() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    Assertions.assertEquals(
        nodes.stream()
            .map(node -> node.getBeanFactory().getBean(TransportServer.class).localNode())
            .distinct()
            .count(),
        3);
  }
}
