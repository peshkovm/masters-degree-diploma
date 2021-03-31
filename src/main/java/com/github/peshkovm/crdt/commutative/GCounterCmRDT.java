package com.github.peshkovm.crdt.commutative;

import com.github.peshkovm.crdt.replication.Replicator;
import io.vavr.control.Option;

/**
 * Increment-only operation based counter.
 */
public class GCounterCmRDT extends CounterCmRDT {

  private final String identity;

  public GCounterCmRDT(String resourceId, Replicator replicator) {
    this.identity = resourceId;
  }

  @Override
  public void increment() {
    super.increment();
  }

  /**
   * Unsupported, because GCounter is increment-only counter.
   */
  @Override
  public void decrement() {
    throw new UnsupportedOperationException("GCounter is increment-only counter");
  }

  @Override
  public void downstream(Option<Long> sourceResult, Long numToAdd) {
    this.i += numToAdd;
  }
}
