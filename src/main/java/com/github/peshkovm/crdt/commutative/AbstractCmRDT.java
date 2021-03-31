package com.github.peshkovm.crdt.commutative;

import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractCmRDT<T, R> implements CmRDT<T, R> {

  protected static final Logger logger = LogManager.getLogger();

  public R query() {
    if (queryPrecondition()) {
      return value();
    } else {
      throw new IllegalStateException("Query precondition is false");
    }
  }

  @Override
  public Option<R> update(T argument) {
    final Option<R> sourceResult = atSource0(argument);
    downstream0(sourceResult, argument);

    return sourceResult;
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
   * @param sourceResult result of atSource operation
   * @param argument {@link CmRDT#update(T) update operation} argument
   */
  private synchronized void downstream0(Option<R> sourceResult, T argument) {
    if (downstreamPrecondition(argument)) {
      downstream(sourceResult, argument); // immediately at the source
      //      replicator.append(
      //          new DownstreamAssign(sourceResult, argument)); // asynchronously, at all other
      // replicas
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
