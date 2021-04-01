package com.github.peshkovm.crdt.replication;

import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.crdt.commutative.protocol.DownstreamUpdate;

public interface Replicator extends LifecycleComponent {

  void append(DownstreamUpdate<?, ?> downstreamUpdate);
}
