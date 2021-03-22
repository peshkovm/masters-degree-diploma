package com.github.peshkovm.raft;

import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.DiscoveryNode;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RaftMetadata {

  @Getter
  private final ClusterDiscovery discovery;

  @Autowired
  public RaftMetadata(ClusterDiscovery discovery) {
    this.discovery = discovery;
  }

  public Set<DiscoveryNode> getDiscoveryNodes() {
    return discovery.getDiscoveryNodes();
  }

  public Set<DiscoveryNode> getDiscoveryNodesWithout(DiscoveryNode member) {
    return discovery.getDiscoveryNodes().stream()
        .filter(node -> !node.equals(member))
        .collect(Collectors.toSet());
  }
}
