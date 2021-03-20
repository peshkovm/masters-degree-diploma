package com.github.peshkovm.raft.cluster;

import com.github.peshkovm.transport.DiscoveryNode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

public class StableClusterConfiguration implements ClusterConfiguration {

  @Getter
  private final Set<DiscoveryNode> members; // Contains all nodes

  public StableClusterConfiguration(DiscoveryNode... members) {
    this(new HashSet<>(Arrays.asList(members)));
  }

  public StableClusterConfiguration(Set<DiscoveryNode> members) {
    this.members = members;
  }
}
