package com.github.peshkovm.crdt.commutative;

import com.github.peshkovm.crdt.Crdt;
import io.vavr.control.Option;
import java.io.Serializable;

/**
 * Defines CmRDT operations.
 *
 * @param <T> type of crdt update operation argument
 * @param <R> type of crdt update method (optional) return value
 */
public interface CmRDT<T extends Serializable, R extends Serializable> extends Crdt<T, R> {

  /**
   * Updates CRDT payload on all replicas.
   *
   * <p>Has two phases:
   *
   * <ul>
   *   <li>First, the client calls the operation at the source, which may perform some initial
   *       processing.
   *   <li>Then, the update is transmitted asynchronously to all replicas; this is the downstream
   *       part
   * </ul>
   *
   * @param argument operation's argument
   * @return result of operation
   */
  Option<R> update(T argument);

  /**
   * Locally updates source replica. The first phase of {@link CmRDT#update(Serializable)} update}
   * operation.
   *
   * <p>
   *
   * <ul>
   *   <li>Enabled only if its (optional) {@link AbstractCmRDT#atSourcePrecondition(Serializable)}
   *       () source pre-condition}, is true in the source state
   *   <li>Executes atomically
   *   <li>Takes its arguments from the operation invocation
   *   <li>Is not allowed to make side effects
   *   <li>May compute results, returned to the caller, and/or prepare arguments for the second
   *       phase
   * </ul>
   *
   * @param argument operation's argument
   * @return computed result
   */
  Option<R> atSource(T argument);

  /**
   * Executes after the source-local phase; immediately at the source, and asynchronously, at all
   * other replicas.
   *
   * <p>
   *
   * <ul>
   *   <li>Executes only if its {@link AbstractCmRDT#downstreamPrecondition(T)} () downstream
   *       precondition} is true
   *   <li>Can not return results
   *   <li>Updates the downstream state
   *   <li>Arguments are those prepared by the source-local phase
   *   <li>Executes atomically
   * </ul>
   *
   * @param argument operation's argument
   */
  void downstream(Option<R> atSourceResult, T argument);
}
