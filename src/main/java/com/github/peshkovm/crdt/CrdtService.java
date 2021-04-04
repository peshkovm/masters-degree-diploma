package com.github.peshkovm.crdt;

import com.github.peshkovm.crdt.registry.CrdtRegistry;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.routing.fsm.AddResourceResponse;
import io.vavr.collection.Vector;
import io.vavr.concurrent.Future;
import java.io.Serializable;

/**
 * Defines methods to service replicated crdt objects.
 */
public interface CrdtService {

  Future<Vector<AddResourceResponse>> addResource(String resourceId, ResourceType resourceType);

  CrdtRegistry crdtRegistry();

  <T extends Serializable, R extends Serializable, M extends Crdt<T, R>>
  Future<Vector<R>> queryAllNodes(String crdtId, Class<M> crdtType);
}
