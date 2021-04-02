package com.github.peshkovm.node;

import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.DiscoveryNode;
import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

/**
 * {@link Node} implementation to use only on different machines, connected via network.
 */
public class ExternalNode extends InternalNode {

  /**
   * Initializes a newly created {@code ExternalNode} object with {@link Config config} argument.
   *
   * @param config config to be used by ExternalNode instance
   */
  public ExternalNode(Config config) {
    super(config);
  }

  @Override
  protected ClusterDiscovery createClusterDiscovery(Config config) {
    Set<DiscoveryNode> replicas = HashSet.empty();
    final DiscoveryNode self =
        new DiscoveryNode(config.getString("transport.host"), config.getInt("transport.port"));

    for (String s : config.getStringList("raft.discovery.external_nodes")) {
      HostAndPort hostAndPort = HostAndPort.fromString(s);
      final String host = hostAndPort.getHost();
      final int port = hostAndPort.getPort();

      final DiscoveryNode replica = new DiscoveryNode(host, port);

      if (!replica.equals(self)) {
        replicas = replicas.add(new DiscoveryNode(host, port));
      }
    }

    return new ClusterDiscovery(self, replicas);
  }
}
