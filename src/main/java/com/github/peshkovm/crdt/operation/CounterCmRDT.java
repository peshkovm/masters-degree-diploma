package com.github.peshkovm.crdt.operation;

import com.github.peshkovm.crdt.replication.Replicator;
import io.vavr.control.Option;

/**
 * An op-based counter. Its payload is an long. Its empty atSource clause is omitted; the downstream
 * phase just adds or subtracts locally. It is wellknown that addition and subtraction commute,
 * assuming no overflow. Therefore, this data type is a CmRDT.
 */
public abstract class CounterCmRDT extends AbstractCmRDT<Long, Long> implements CounterCRDT {

  protected Long i; // Immutable payload

  /**
   * Instantiates new CmRDT counter instance.
   *
   * @param identity crdt object identity, for example "countOfLikes"
   * @param replicator {@link Replicator} instance
   */
  public CounterCmRDT(String identity, Replicator replicator) {
    super(identity, replicator);
    this.i = 0L; // initial payload value
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
    return i;
  }

  /** Empty. */
  @Override
  public synchronized Option<Long> atSource(Long argument) {
    return Option.none();
  }

  /** Always true */
  @Override
  protected boolean atSourcePrecondition(Long argument) {
    return true;
  }

  /** Empty */
  @Override
  protected Option<Long> atSourceImpl(Long argument) {
    return Option.none();
  }
}
