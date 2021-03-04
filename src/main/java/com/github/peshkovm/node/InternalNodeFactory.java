package com.github.peshkovm.node;

import com.github.peshkovm.common.config.ConfigBuilder;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Factory class to creates {@link InternalNode} instances.
 */
public final class InternalNodeFactory {

  private static final Set<Integer> ports = Sets.newHashSet(); // List of cluster nodes ports
  private static final ApplicationContext applicationContext =
      new AnnotationConfigApplicationContext(InternalNodeConfiguration.class);

  private InternalNodeFactory() {
  }

  private static int nextPort() {
    for (; ; ) {
      int port = 8800 + ThreadLocalRandom.current().nextInt(100);
      if (!ports.contains(port)) {
        ports.add(port);
        return port;
      }
    }
  }

  /**
   * Creates leader node on local host with random port.
   *
   * @return newly created InternalNode instance
   */
  public static InternalNode createLeaderNode() {
    final int port = nextPort();
    final Config config =
        new ConfigBuilder().with("transport.port", port).with("transport.is_leader", true).build();

    final InternalNode node = applicationContext.getBean(InternalNode.class);
    node.setConfig(config);

    return node;
  }

  public static void reset() {
    ports.clear();
  }

  /**
   * Creates follower node on local host with random port.
   *
   * @return newly created InternalNode instance
   */
  public static InternalNode createFollowerNode() {
    final int port = nextPort();
    final Config config =
        new ConfigBuilder().with("transport.port", port).with("transport.is_leader", false).build();

    final InternalNode node = applicationContext.getBean(InternalNode.class);
    node.setConfig(config);

    return node;
  }
}
