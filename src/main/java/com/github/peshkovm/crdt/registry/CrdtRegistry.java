package com.github.peshkovm.crdt.registry;

import com.github.peshkovm.crdt.Crdt;

/** Defines methods for creating and receiving replicated crdt objects. */
public interface CrdtRegistry {

  boolean createGCounterCmRDT(String resourceId);

  boolean createGCounterCvRDT(String resourceId);

  <T extends Crdt<?, ?>> T crdt(String crdtId, Class<T> crdtType);
}
