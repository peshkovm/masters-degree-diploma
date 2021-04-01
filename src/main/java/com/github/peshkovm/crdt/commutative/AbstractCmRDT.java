package com.github.peshkovm.crdt.commutative;

import com.github.peshkovm.crdt.commutative.protocol.DownstreamUpdate;
import com.github.peshkovm.crdt.replication.Replicator;
import io.vavr.control.Option;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract class with default implementation of {@link CmRDT} methods.
 *
 * @param <T> type of crdt update operation argument
 * @param <R> type of crdt update method (optional) return value
 */
public abstract class AbstractCmRDT<T extends Serializable, R extends Serializable>
    implements CmRDT<T, R> {

  protected static final Logger logger = LogManager.getLogger();
  protected final String identity; // object's identity
  private final Replicator replicator; // allows to asynchronously transmit update to all replicas
  private final Class<AbstractCmRDT<T, R>> type = (Class<AbstractCmRDT<T, R>>) this.getClass();

  public AbstractCmRDT(String identity, Replicator replicator) {
    this.identity = identity;
    this.replicator = replicator;
  }

  public R query() {
    if (queryPrecondition()) {
      return value();
    } else {
      throw new IllegalStateException("Query precondition is false");
    }
  }

  @Override
  public Option<R> update(T argument) {
    final Option<R> atSourceResult = atSource0(argument);
    downstream0(atSourceResult, argument);

    return atSourceResult;
  }

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
   * @return result of operation
   */
  protected abstract R value();

  /**
   * Locally updates source replica. The first phase of {@link CmRDT#update(T)} update} operation.
   *
   * <p>
   *
   * <ul>
   *   <li>Enabled only if its (optional) {@link AbstractCmRDT#atSourcePrecondition(T)} () source
   *       pre-condition}, is true in the source state
   *   <li>Executes atomically
   *   <li>Takes its arguments from the operation invocation
   *   <li>Is not allowed to make side effects
   *   <li>May compute results, returned to the caller, and/or prepare arguments for the second
   *       phase
   * </ul>
   *
   * @param argument {@link CmRDT#update(T) update operation} argument
   * @return computed result
   */
  private synchronized Option<R> atSource0(T argument) {
    if (atSourcePrecondition(argument)) {
      return atSource(argument);
    } else {
      throw new IllegalStateException("At source precondition is false");
    }
  }

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
   * @param atSourceResult result of atSource operation
   * @param argument {@link CmRDT#update(T) update operation} argument
   */
  private synchronized void downstream0(Option<R> atSourceResult, T argument) {
    if (downstreamPrecondition(argument)) {
      downstream(atSourceResult, argument); // immediately at the source
      replicator.append(
          new DownstreamUpdate<>(
              this.identity,
              atSourceResult,
              type,
              argument)); // asynchronously, at all other replicas
    } else {
      throw new IllegalStateException("Downstream precondition is false");
    }
  }

  /**
   * The {@link CmRDT#query() query operation} will be enabled only if this pre-condition returns
   * true.
   *
   * @return Is query pre-condition holds in the source’s current state
   */
  protected boolean queryPrecondition() {
    return true;
  }

  /**
   * The {@link AbstractCmRDT#atSource(T) atSource operation} will be enabled only if this
   * pre-condition returns true.
   *
   * @param argument atSource operation argument
   * @return Is atSource pre-condition holds in the source’s current state
   */
  protected boolean atSourcePrecondition(T argument) {
    return true;
  }

  /**
   * The {@link AbstractCmRDT#downstream(Option, T) downstream operation} will be enabled only if
   * this pre-condition returns true.
   *
   * @param argument downstream operation argument
   * @return Is downstream pre-condition holds in the source’s current state
   */
  protected boolean downstreamPrecondition(T argument) {
    return true;
  }
}
