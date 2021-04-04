package com.github.peshkovm.crdt;

import com.github.peshkovm.crdt.registry.CrdtRegistry;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.routing.fsm.AddResourceResponse;
import io.vavr.concurrent.Future;

/**
 * Defines methods to service replicated crdt objects.
 */
public interface CrdtService {

  Future<AddResourceResponse> addResource(String resourceId, ResourceType resourceType);

  CrdtRegistry crdtRegistry();
}
