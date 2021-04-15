package com.github.peshkovm.crdt.operationbased;

import com.github.peshkovm.crdt.replication.Replicator;
import io.vavr.control.Option;

/** Increment-only operation based counter. */
public class GCounterCmRDT extends CounterCmRDT {

  /**
   * Instantiates new CmRDT increment-only counter instance.
   *
   * @param identity crdt object identity, for example "countOfLikes"
   * @param replicator {@link Replicator} instance
   */
  public GCounterCmRDT(String identity, Replicator replicator) {
    super(identity, replicator);
  }

  @Override
  public void increment() {
    super.increment();
  }

  /** Unsupported, because GCounter is increment-only counter. */
  @Override
  public void decrement() {
    throw new UnsupportedOperationException("GCounter is increment-only counter");
  }

  @Override
  protected void downstreamImpl(Option<Long> atSourceResult, Long numToAdd) {
    logger.debug("downstream phase received {}, {}", () -> atSourceResult, () -> numToAdd);
    this.i += numToAdd;
  }

  @Override
  protected boolean downstreamPrecondition(Option<Long> atSourceResult, Long argument) {
    return argument >= 0;
  }
}
