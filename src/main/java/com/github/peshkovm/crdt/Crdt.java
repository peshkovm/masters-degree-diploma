package com.github.peshkovm.crdt;

import io.vavr.control.Option;
import java.io.Serializable;

/**
 * Defines CRDT operations.
 *
 * <p>
 *
 * @param <T> type of crdt update operation argument
 * @param <R> type of crdt query method return value
 * @see <a href="https://hal.inria.fr/inria-00555588/document">A comprehensive study of Convergent
 *     and Commutative Replicated Data Types</a>
 */
public interface Crdt<T extends Serializable, R extends Serializable> {

  /**
   * Returns CDRT payload.
   *
   * <p>
   *
   * <ul>
   *   <li>Does not mutate the state
   *   <li>Executes entirely at a single replica
   * </ul>
   *
   * @return object's payload
   */
  R query();

  /**
   * Updates CRDT payload on all replicas.
   *
   * <p>
   *
   * <ul>
   *   <li>Mutates the state
   *   <li>Executes on all replicas
   * </ul>
   *
   * @param argument operation's argument
   * @return result of operation
   */
  Option<R> update(T argument);
}
