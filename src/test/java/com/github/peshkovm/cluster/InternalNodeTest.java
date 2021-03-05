package com.github.peshkovm.cluster;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.node.InternalClusterFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InternalNodeTest extends BaseClusterTest {

  @Test
  @DisplayName("Should start then stop and close leader node")
  void shouldStartThenStopAndCloseLeaderNode() {
    createAndStartLeader();
    final InternalNode leaderNode = nodes.get(0);

    Assertions.assertTrue(leaderNode.isStarted());
    Assertions.assertTrue(leaderNode.isStarted());
    leaderNode.stop();
    Assertions.assertTrue(leaderNode.isStopped());
    leaderNode.close();
    Assertions.assertTrue(leaderNode.isClosed());
  }

  @Test
  @DisplayName("Should start then stop and close follower node")
  void shouldStartThenStopAndCloseFollowerNode() {
    createAndStartFollower();
    final InternalNode followerNode = nodes.get(0);

    Assertions.assertTrue(followerNode.isStarted());
    followerNode.stop();
    Assertions.assertTrue(followerNode.isStopped());
    followerNode.close();
    Assertions.assertTrue(followerNode.isClosed());
  }

  @Test
  @DisplayName("Should create all nodes on localhost")
  void shouldCreateAllNodesOnLocalhost() {
    createAndStartLeader();
    createAndStartFollower();
    createAndStartFollower();
    createAndStartFollower();

    Assertions.assertEquals(
        nodes.stream()
            .map(internalNode -> internalNode.getHostAndPort().getHost())
            .distinct()
            .collect(Collectors.toList()),
        Collections.singletonList("127.0.0.1"));
  }

  @Test
  @DisplayName("Should create all nodes on different ports")
  void shouldCreateAllNodesOnDifferentPorts() {
    createAndStartLeader();
    createAndStartFollower();
    createAndStartFollower();
    createAndStartFollower();

    Assertions.assertEquals(nodes.stream().map(InternalNode::getHostAndPort).distinct().count(), 4);
  }
}
