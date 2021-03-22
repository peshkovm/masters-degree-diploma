package com.github.peshkovm.raft.discovery;

import com.github.peshkovm.transport.DiscoveryNode;
import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
    final HashSet<DiscoveryNode> nodes = new HashSet<DiscoveryNode>(Collections.singleton(self));

    if (config.hasPath("raft.discovery.nodes")) {
      config
          .getStringList("raft.discovery.nodes")
          .forEach(
              nodeAddress -> {
                final HostAndPort hostAndPort = HostAndPort.fromString(nodeAddress);

                nodes.add(new DiscoveryNode(hostAndPort.getHost(), hostAndPort.getPort()));
              });
    }

    logger.info("Nodes: {}", nodes);

    return nodes;
  }
}
