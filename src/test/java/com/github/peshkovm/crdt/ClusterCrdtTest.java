package com.github.peshkovm.crdt;

import com.github.peshkovm.common.BaseClusterTest;
import org.junit.jupiter.api.BeforeEach;

public class ClusterCrdtTest extends BaseClusterTest {

  @BeforeEach
  public void setUpNodes() {
    createLeader();
    createFollower();
    createFollower();
  }

  public void testLWWRegister() {
  }
}
