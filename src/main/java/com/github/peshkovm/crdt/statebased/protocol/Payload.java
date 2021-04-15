package com.github.peshkovm.crdt.statebased.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.crdt.statebased.CvRDT;
import java.io.Serializable;
import lombok.Data;

/** Payload that will be transmitted asynchronously to all replicas. */
@Data
public class Payload<T extends Serializable, R extends Serializable, U extends Serializable>
    implements Message {

  private final U payload;
  private final String crdtId;
  private final Class<? extends CvRDT<T, R, U>> crdtType;

  public Payload(U payload, String crdtId, Class<? extends CvRDT<T, R, U>> crdtType) {
    this.payload = payload;
    this.crdtId = crdtId;
    this.crdtType = crdtType;
  }
}
