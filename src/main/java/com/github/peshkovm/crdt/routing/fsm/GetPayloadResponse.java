package com.github.peshkovm.crdt.routing.fsm;

import com.github.peshkovm.crdt.Crdt;
import java.io.Serializable;
import lombok.Data;

@Data
public class GetPayloadResponse<T extends Serializable, R extends Serializable> {

  private final String crdtId;
  private final Class<? extends Crdt<T, R>> crdtType;
  private final R payload;

  public <M extends Crdt<T, R>> GetPayloadResponse(String crdtId, Class<M> crdtType, R payload) {
    this.crdtId = crdtId;
    this.crdtType = crdtType;
    this.payload = payload;
  }
}
