package com.github.peshkovm.crdt;

import io.vavr.control.Option;
import java.io.Serializable;

/**
 * Interface that defines basic CRDT operations.
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
