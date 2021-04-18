package com.github.peshkovm.crdt.basic;

import com.github.peshkovm.crdt.Crdt;

/**
 * A Counter is a replicated long supporting operations increment and decrement to update it, and
 * value to query it. The semantics should be is that the value converge towards the global number
 * of increments minus the number of decrements. (Extension to operations for adding and subtracting
 * an argument is straightforward.) A Counter CRDT is useful in many peer-to-peer applications, for
 * instance counting the number of currently logged-in users.
 */
public interface CounterCRDT extends Crdt<Long, Long> {

  /** Increments counter's payload. */
  void increment();

  /** Decrements counter's payload. */
  void decrement();

  /**
   * Returns counter's payload
   *
   * @return
   */
  Long value();
}
