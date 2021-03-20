package com.github.peshkovm.raft.cluster;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import java.util.Set;

public interface ClusterConfiguration extends Message {

  Set<DiscoveryNode> getMembers();
}
