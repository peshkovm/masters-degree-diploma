package com.github.peshkovm.crdt.replication;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.component.LifecycleComponent;

/**
 * Defines methods to replicate all crdt downstream updates or payloads to all replicas by reliable
 * broadcast channel, that guaranties that all updates are delivered at every replica, in the
 * delivery order.
 */
public interface Replicator extends LifecycleComponent {

  /**
   * Replicates CRDT downstream update or payload to all replicas.
   *
   * @param message transmitted message
   */
  void replicate(Message message);
}
