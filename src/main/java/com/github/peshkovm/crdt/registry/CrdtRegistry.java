package com.github.peshkovm.crdt.registry;

import com.github.peshkovm.crdt.Crdt;

public interface CrdtRegistry {

  boolean createGCounter(String resourceId);

  Crdt<?, ?> crdt(String crdtId);

  <T extends Crdt<?, ?>> T crdt(String crdtId, Class<T> crdtType);
}
