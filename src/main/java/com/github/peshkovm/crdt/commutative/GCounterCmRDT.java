package com.github.peshkovm.crdt.commutative;

import com.github.peshkovm.crdt.replication.Replicator;
import io.vavr.control.Option;

/**
 * Increment-only operation based counter.
 */
public class GCounterCmRDT extends CounterCmRDT {

  public GCounterCmRDT(String resourceId, Replicator replicator) {
    super(resourceId, replicator);
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
  public void downstream(Option<Long> atSourceResult, Long numToAdd) {
    this.i += numToAdd;
  }
}
