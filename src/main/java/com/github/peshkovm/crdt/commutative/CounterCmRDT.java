package com.github.peshkovm.crdt.commutative;

import com.github.peshkovm.crdt.replication.Replicator;
import io.vavr.control.Option;

/**
 * An op-based counter. Its payload is an long. Its empty atSource clause is omitted; the downstream
 * phase just adds or subtracts locally. It is wellknown that addition and subtraction commute,
 * assuming no overflow. Therefore, this data type is a CmRDT.
 */
public abstract class CounterCmRDT extends AbstractCmRDT<Long, Long> implements CounterCRDT {

  protected Long i; // Immutable payload

  public CounterCmRDT(String resourceId, Replicator replicator) {
    super(resourceId, replicator);
    this.i = 0L; // initial payload state
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
  protected Long value() {
    return i;
  }

  @Override
  public Option<Long> atSource(Long argument) {
    final Option<Long> result = Option.none();

    logger.debug("atSource phase received {} and returned {}", () -> argument, () -> result);
    return result;
  }
}
