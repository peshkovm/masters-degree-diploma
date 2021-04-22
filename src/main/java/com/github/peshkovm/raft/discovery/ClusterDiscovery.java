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
  @Getter private final DiscoveryNode self;
  @Getter private final Set<DiscoveryNode> replicas;

  @Autowired
  public ClusterDiscovery(Config config) {
    Set<DiscoveryNode> replicas = HashSet.empty();
    final DiscoveryNode self =
        new DiscoveryNode(config.getString("transport.host"), config.getInt("transport.port"));

    for (String s : config.getStringList("raft.discovery.internal_nodes")) {
      HostAndPort hostAndPort = HostAndPort.fromString(s);
      final String host = hostAndPort.getHost();
      final int port = hostAndPort.getPort();

      final DiscoveryNode replica = new DiscoveryNode(host, port);

      if (!replica.equals(self)) {
        replicas = replicas.add(new DiscoveryNode(host, port));
      }
    }

    this.self = self;
    this.replicas = replicas;

    logger.info("Self = {}", () -> this.self);
    logger.info("Replicas = {}", () -> this.replicas);
  }

  public Set<DiscoveryNode> getDiscoveryNodes() {
    HashSet<DiscoveryNode> discoveryNodes = HashSet.of(self);
    discoveryNodes = discoveryNodes.addAll(replicas);

    return discoveryNodes;
  }
}
