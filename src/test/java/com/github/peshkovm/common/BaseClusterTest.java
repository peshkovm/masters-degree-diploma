package com.github.peshkovm.common;

import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.node.InternalNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.AfterEach;

/**
 * Provides methods for cluster testing.
 */
public class BaseClusterTest {

  private List<InternalNode> nodes = Lists.newArrayList();
  private Set<Integer> ports = Sets.newHashSet(); // List of cluster nodes ports

  private int nextPort() {
    for (; ; ) {
      int port = 8800 + ThreadLocalRandom.current().nextInt(100);
      if (!ports.contains(port)) {
        ports.add(port);
        return port;
      }
    }
  }

  /**
   * Creates leader node on local host with random port
   */
  protected final void createLeader() {
    final int leaderPort = nextPort();
    final Config config = new ConfigBuilder().with("transport.port", leaderPort).build();
    final InternalNode node = new InternalNode(config);
    node.start();
    nodes.add(node);
  }

  /**
   * Creates follower node on local host with random port
   */
  protected final void createFollower() {
    final int port = nextPort();
    final Config config = new ConfigBuilder().with("transport.port", port).build();
    final InternalNode node = new InternalNode(config);
    node.start();
    nodes.add(node);
  }

  @AfterEach
  protected void tearDownNodes() {
    nodes.forEach(LifecycleComponent::stop);
    nodes.forEach(LifecycleComponent::close);
    nodes = Lists.newArrayList();
    ports = Sets.newHashSet();
  }
}
