package com.github.peshkovm.raft.discovery;

import com.github.peshkovm.transport.DiscoveryNode;
import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClusterDiscovery {

  private static final Logger logger = LogManager.getLogger();
  @Getter
  private final DiscoveryNode self;
  @Getter
  private final Set<DiscoveryNode> discoveryNodes;

  @Autowired
  public ClusterDiscovery(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") Config config) {
    self = new DiscoveryNode(config.getString("transport.host"), config.getInt("transport.port"));
    discoveryNodes = parseDiscovery(config);
  }

  private Set<DiscoveryNode> parseDiscovery(Config config) {
    Set<DiscoveryNode> nodes = HashSet.of(self);

    if (config.hasPath("raft.discovery.nodes")) {
      for (String nodeAddress : config.getStringList("raft.discovery.nodes")) {
        final HostAndPort hostAndPort = HostAndPort.fromString(nodeAddress);

        nodes = nodes.add(new DiscoveryNode(hostAndPort.getHost(), hostAndPort.getPort()));
      }
    }

    logger.info("Nodes: {}", nodes);

    return nodes;
  }
}
