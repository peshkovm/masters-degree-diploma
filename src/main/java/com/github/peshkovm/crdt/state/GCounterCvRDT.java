package com.github.peshkovm.crdt.state;

import com.github.peshkovm.crdt.operation.CounterCRDT;
import com.github.peshkovm.crdt.replication.Replicator;
import io.vavr.collection.Vector;
import io.vavr.control.Option;

/** Increment-only state based counter. */
public class GCounterCvRDT extends AbstractCvRDT<Long, Long, Vector<Long>> implements CounterCRDT {
  protected Vector<Long> integers; // Immutable payload
  protected final int id;

  /**
   * Instantiates new CvRDT increment-only counter instance.
   *
   * @param identity crdt object identity, for example "countOfLikes"
   * @param amountOfNodes amount of nodes in cluster
   * @param id node's id in cluster
   * @param replicator {@link Replicator} instance
   */
  public GCounterCvRDT(String identity, int amountOfNodes, int id, Replicator replicator) {
    super(identity, replicator);
    this.integers = Vector.fill(amountOfNodes, 0L); // initial payload value
    this.id = id;
  }

  @Override
  public void increment() {
    update(1L);
  }

  @Override
  public void decrement() {
    update(-1L);
  }

  @Override
  protected Long queryImpl() {
    return integers.sum().longValue();
  }

  @Override
  protected Option<Long> updateImpl(Long argument) {
    integers = integers.update(id, integers.get(id) + 1);
    return Option.none();
  }

  @Override
  protected boolean compareImpl(Vector<Long> localPayload, Vector<Long> replicaPayload) {
    for (int i = 0; i < localPayload.size(); i++) {
      if (replicaPayload.get(i) > localPayload.get(i)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected Vector<Long> mergeImpl(Vector<Long> localPayload, Vector<Long> replicaPayload) {
    Vector<Long> z = Vector.fill(localPayload.size(), 0L);

    for (int i = 0; i < localPayload.size(); i++) {
      z = z.update(i, Math.max(localPayload.get(i), replicaPayload.get(i)));
    }

    return z;
  }

  @Override
  protected Vector<Long> getPayload() {
    return integers;
  }

  @Override
  protected void setPayload(Vector<Long> mergedPayload) {
    integers = mergedPayload;
  }
}
