package com.github.peshkovm.crdt.state;

import com.github.peshkovm.crdt.Crdt;
import io.vavr.control.Option;
import java.io.Serializable;

/**
 * Defines CvRDT operations
 *
 * @param <T> type of crdt update operation (optional) argument
 * @param <R> type of crdt query operation return value
 */
public interface CvRDT<T extends Serializable, R extends Serializable, U extends Serializable>
    extends Crdt<T, R> {

  /**
   * Updates CRDT payload on all replicas.
   *
   * <p>Occurs entirely at source, then propagates by transmitting the modified payload between
   * replicas. Executes atomically.
   *
   * @param argument operation's argument
   * @return optional result of operation
   */
  @Override
  Option<R> update(T argument);

  /**
   * Compares local payload with payload received from replica.
   *
   * <p><i>Is value1 ≤ value2 in semilattice?</i>
   *
   * @param replicaPayload payload received from replica
   * @return true if localPayload ≤ replicaPayload in semilattice, false otherwise
   */
  boolean compare(U replicaPayload);

  /**
   * Merges local payload with payload received from replica.
   *
   * <p><i>LUB merge of value1 and value2, at any replica</i>
   *
   * @param replicaPayload payload received from replica
   * @return LUB of {localPayload, replicaPayload}
   */
  U merge(U replicaPayload);
}
