package com.github.peshkovm.crdt.registry;

import com.github.peshkovm.crdt.Crdt;
import com.github.peshkovm.crdt.operationbased.GCounterCmRDT;

/** Defines methods for creating and receiving replicated crdt objects. */
public interface CrdtRegistry {

  boolean createGCounterCmRDT(String resourceId);

  boolean createGCounterCvRDT(String resourceId);

  boolean deleteCRDT(String resourceId, Class<? extends Crdt<?, ?>> resourceClass);

  <T extends Crdt<?, ?>> T crdt(String crdtId, Class<T> crdtType);
}
