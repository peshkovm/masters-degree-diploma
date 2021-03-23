package com.github.peshkovm.raft;

import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.DiscoveryNode;
import io.vavr.collection.Set;
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
    return getDiscoveryNodes().remove(member);
  }
}
