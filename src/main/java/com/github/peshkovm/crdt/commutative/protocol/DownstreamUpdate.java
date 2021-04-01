package com.github.peshkovm.crdt.commutative.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.crdt.commutative.CmRDT;
import io.vavr.control.Option;
import java.io.Serializable;
import lombok.Data;

/**
 * Downstream update that will be transmitted asynchronously to all replicas.
 */
@Data
public class DownstreamUpdate<T extends Serializable, R extends Serializable> implements Message {

  private final String crdtId;
  private final Option<R> atSourceResult;
  private final Class<? extends CmRDT<T, R>> crdtType;
  private final T argument;

  public DownstreamUpdate(
      String crdtId, Option<R> atSourceResult, Class<? extends CmRDT<T, R>> crdtType, T argument) {

    this.crdtId = crdtId;
    this.atSourceResult = atSourceResult;
    this.crdtType = crdtType;
    this.argument = argument;
  }
}
