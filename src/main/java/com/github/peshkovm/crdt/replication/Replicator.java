package com.github.peshkovm.crdt.replication;

import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.crdt.commutative.protocol.DownstreamUpdate;
import io.vavr.control.Option;
import java.io.Serializable;

/**
 * Defines methods to replicate all downstream updates to all replicas by reliable broadcast
 * channel, that guaranties that all updates are delivered at every replica, in the delivery order.
 */
public interface Replicator extends LifecycleComponent {

  /**
   * Replicates {@link com.github.peshkovm.crdt.commutative.CmRDT#downstream(Option, Serializable)
   * CmRDT downstream phase} to all replicas.
   *
   * @param downstreamUpdate transmitted downstream update
   */
  void append(DownstreamUpdate<?, ?> downstreamUpdate);
}
