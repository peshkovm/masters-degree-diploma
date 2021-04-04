package com.github.peshkovm.crdt.routing.fsm;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.crdt.Crdt;
import java.io.Serializable;
import lombok.Data;

@Data
public class GetPayload<T extends Serializable, R extends Serializable> implements Message {

  private final String crdtId;
  private final Class<? extends Crdt<T, R>> crdtType;

  public <M extends Crdt<T, R>> GetPayload(String crdtId, Class<M> crdtType) {
    this.crdtId = crdtId;
    this.crdtType = crdtType;
  }
}
