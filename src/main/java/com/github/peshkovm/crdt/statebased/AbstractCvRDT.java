package com.github.peshkovm.crdt.statebased;

import com.github.peshkovm.crdt.Crdt;
import com.github.peshkovm.crdt.replication.Replicator;
import com.github.peshkovm.crdt.statebased.protocol.Payload;
import io.vavr.control.Option;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract class with default implementation of {@link CvRDT} methods.
 *
 * @param <T> type of crdt update operation (optional) argument
 * @param <R> type of crdt query operation return value
 * @param <U> type of payload
 */
public abstract class AbstractCvRDT<
        T extends Serializable, R extends Serializable, U extends Serializable>
    implements CvRDT<T, R, U> {

  protected static final Logger logger = LogManager.getLogger();
  protected final String identity; // object's identity
  private final Replicator
      replicator; // allows to asynchronously transmit modified payload between replicas
  private final Class<AbstractCvRDT<T, R, U>> type =
      (Class<AbstractCvRDT<T, R, U>>) this.getClass();

  /**
   * Instantiates new {@link CvRDT} object instance.
   *
   * @param identity crdt object identity, for example "countOfLikes"
   * @param replicator {@link Replicator} instance
   */
  public AbstractCvRDT(String identity, Replicator replicator) {
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
  public synchronized Option<R> update(T argument) {
    if (updatePrecondition(argument)) {
      return updateImpl(argument);
    } else {
      logger.warn("Update precondition is false");
      return Option.none();
    }
  }

  @Override
  public synchronized boolean compare(U replicaPayload) {
    final U localPayload = getPayload();
    return compareImpl(localPayload, replicaPayload);
  }

  @Override
  public synchronized U merge(U replicaPayload) {
    final U localPayload = getPayload();
    final U mergedPayload = mergeImpl(localPayload, replicaPayload);
    setPayload(mergedPayload);

    return mergedPayload;
  }

  protected boolean updatePrecondition(T argument) {
    return true;
  }

  /**
   * The {@link Crdt#query()} query operation} will be enabled only if this pre-condition returns
   * true.
   *
   * @return Is query pre-condition holds in the source’s current state
   */
  protected boolean queryPrecondition() {
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
   * {@link CvRDT#update(Serializable) update operation}'s logic.
   *
   * <p>Evaluate at source, synchronously. Side-effects at source to execute synchronously
   *
   * @param argument {@link CvRDT#update(Serializable) update operation} argument
   * @return optional result of operation
   */
  protected abstract Option<R> updateImpl(T argument);

  /**
   * {@link CvRDT#compare(Serializable) compare operation}'s logic.
   *
   * @param localPayload {@link CvRDT#compare(Serializable) compare operation} argument
   * @param replicaPayload {@link CvRDT#compare(Serializable) compare operation} argument
   * @return true if localPayload ≤ replicaPayload in semilattice, false otherwise
   */
  protected abstract boolean compareImpl(U localPayload, U replicaPayload);

  /**
   * {@link CvRDT#merge(Serializable) merge operation}'s logic.
   *
   * @param localPayload {@link CvRDT#merge(Serializable) merge operation} argument
   * @param replicaPayload {@link CvRDT#merge(Serializable) merge operation} argument
   * @return LUB of {localPayload, replicaPayload}
   */
  protected abstract U mergeImpl(U localPayload, U replicaPayload);

  /** Transmits payload between arbitrary pairs of replicas, in order to propagate changes. */
  public synchronized void replicatePayload() {
    replicator.replicate(new Payload<>(getPayload(), this.identity, type));
  }

  protected abstract U getPayload();

  protected abstract void setPayload(U mergedPayload);
}
