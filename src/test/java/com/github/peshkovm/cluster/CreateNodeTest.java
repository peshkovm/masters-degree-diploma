package com.github.peshkovm.cluster;

import com.github.peshkovm.common.BaseClusterTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CreateNodeTest extends BaseClusterTest {

  @Test
  @DisplayName("Should create leader node")
  void shouldCreateLeaderNode() {
    createLeader();
  }

  @Test
  @DisplayName("Should create follower node")
  void shouldCreateFollowerNode() {
    createFollower();
  }
}
