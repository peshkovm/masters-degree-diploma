package com.github.peshkovm.crdt.operationbased;

import com.github.peshkovm.crdt.basic.RegisterCRDT;
import com.github.peshkovm.crdt.replication.Replicator;
import io.vavr.control.Option;

public class LWWRegisterCmRDT extends AbstractCmRDT<Long, Long>
    implements RegisterCRDT<Long, Long> {

  private Long x; // immutable payload
  private Long timestamp;
  /**
   * Instantiates new CmRDT register instance.
   *
   * @param identity crdt object identity, for example "totalPrice"
   * @param replicator {@link Replicator} instance
   */
  public LWWRegisterCmRDT(String identity, Replicator replicator) {
    super(identity, replicator);
    this.x = 0L;
    this.timestamp = 0L;
  }

  @Override
  public void assign(Long value) {
    update(value);
  }

  @Override
  public Long value() {
    return query();
  }

  @Override
  protected Long queryImpl() {
    return x;
  }

  @Override
  protected Option<Long> atSourceImpl(Long argument) {
    return Option.of(now());
  }

  @Override
  protected void downstreamImpl(Option<Long> atSourceResult, Long argument) {
    final Long replicaTimestamp = atSourceResult.get();

    if (timestamp < replicaTimestamp) {
      timestamp = replicaTimestamp;
      x = argument;
    }
  }

  protected long now() {
    return System.nanoTime();
  }
}
