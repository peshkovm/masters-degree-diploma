package com.github.peshkovm.crdt.operationbased;

import com.github.peshkovm.crdt.Crdt;
import com.github.peshkovm.crdt.operationbased.protocol.DownstreamUpdate;
import com.github.peshkovm.crdt.replication.Replicator;
import io.vavr.control.Option;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract class with default implementation of {@link CmRDT} methods.
 *
 * @param <T> type of crdt update operation argument
 * @param <R> type of crdt query operation return value
 */
public abstract class AbstractCmRDT<T extends Serializable, R extends Serializable>
    implements CmRDT<T, R> {

  protected static final Logger logger = LogManager.getLogger();
  protected final String identity; // object's identity
  private final Replicator replicator; // allows to asynchronously transmit update to all replicas
  private final Class<AbstractCmRDT<T, R>> type = (Class<AbstractCmRDT<T, R>>) this.getClass();

  /**
   * Instantiates new {@link CmRDT} instance.
   *
   * @param identity crdt object identity, for example "countOfLikes"
   * @param replicator {@link Replicator} instance
   */
  public AbstractCmRDT(String identity, Replicator replicator) {
    this.identity = identity;
    this.replicator = replicator;
  }

  @Override
  public R query() {
    if (queryPrecondition()) {
      return queryImpl();
    } else {
      logger.warn("Query precondition is false");
      return null;
    }
  }

  @Override
  public Option<R> update(T argument) {
    final Option<R> atSourceResult = atSource(argument);
    downstream(atSourceResult, argument); // immediately at the source
    replicateDownstream(atSourceResult, argument); // asynchronously, at all other replicas

    return atSourceResult;
  }

  private void replicateDownstream(Option<R> atSourceResult, T argument) {
    replicator.replicate(new DownstreamUpdate<>(this.identity, atSourceResult, type, argument));
  }

  @Override
  public synchronized Option<R> atSource(T argument) {
    if (atSourcePrecondition(argument)) {
      return atSourceImpl(argument);
    } else {
      logger.warn("At source precondition is false");
      return Option.none();
    }
  }

  @Override
  public synchronized void downstream(Option<R> atSourceResult, T argument) {
    if (downstreamPrecondition(atSourceResult, argument)) {
      downstreamImpl(atSourceResult, argument);
    } else {
      logger.warn("Downstream precondition is false");
    }
  }

  /**
   * The {@link Crdt#query() query operation} will be enabled only if this pre-condition returns
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
   * @param argument {@link CmRDT#atSource atSource operation} argument
   * @return Is atSource pre-condition holds in the source’s current state
   */
  protected boolean atSourcePrecondition(T argument) {
    return true;
  }

  /**
   * The {@link AbstractCmRDT#downstream(Option, T) downstream operation} will be enabled only if
   * this pre-condition returns true.
   *
   * @param atSourceResult downstream operation argument
   * @param argument downstream operation argument
   * @return Is downstream pre-condition holds in the source’s current state
   */
  protected boolean downstreamPrecondition(Option<R> atSourceResult, T argument) {
    return true;
  }

  /**
   * {@link Crdt#query() query operation}'s logic.
   *
   * <p>Execute at source, synchronously, no side effects
   *
   * @return object's payload
   */
  protected abstract R queryImpl();

  /**
   * {@link CmRDT#atSource(Serializable) atSource operation}'s logic.
   *
   * <p>Synchronous, at source, no side effects
   *
   * @param argument {@link CmRDT#update(T) update operation} argument
   * @return computed result
   */
  protected abstract Option<R> atSourceImpl(T argument);

  /**
   * {@link CmRDT#downstream(Option, Serializable) downstream operation}'s logic.
   *
   * <p>Asynchronous, side-effects to downstream state
   *
   * @param atSourceResult result of {@link CmRDT#atSource(Serializable) atSource} operation
   * @param argument {@link CmRDT#update(T) update operation} argument
   */
  protected abstract void downstreamImpl(Option<R> atSourceResult, T argument);
}
