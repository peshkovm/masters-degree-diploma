package com.github.peshkovm.raft.discovery;

import com.github.peshkovm.transport.DiscoveryNode;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClusterDiscovery {

  private static final Logger logger = LogManager.getLogger();
  @Getter
  private final DiscoveryNode self;
  @Getter
  private final Set<DiscoveryNode> replicas;

  public ClusterDiscovery(
      DiscoveryNode selfDiscoveryNode, Set<DiscoveryNode> replicasDiscoveryNodes) {
    this.self = selfDiscoveryNode;
    this.replicas = replicasDiscoveryNodes;

    logger.info("Self = {}", () -> self);
    logger.info("Replicas = {}", () -> replicas);
  }

  public Set<DiscoveryNode> getDiscoveryNodes() {
    HashSet<DiscoveryNode> discoveryNodes = HashSet.of(self);
    discoveryNodes = discoveryNodes.addAll(replicas);

    return discoveryNodes;
  }
}
