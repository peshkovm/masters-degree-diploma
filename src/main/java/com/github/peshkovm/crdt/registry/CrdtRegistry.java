package com.github.peshkovm.crdt.registry;

import com.github.peshkovm.crdt.Crdt;
import com.github.peshkovm.crdt.routing.ResourceType;

public interface CrdtRegistry {

  boolean createGCounter(String resourceId);

  <T extends Crdt> T crdt(String crdtId, Class<T> crdtType);
}
