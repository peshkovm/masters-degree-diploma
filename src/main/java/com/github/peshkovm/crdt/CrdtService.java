package com.github.peshkovm.crdt;

import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.routing.fsm.AddResourceResponse;
import io.vavr.concurrent.Future;

public interface CrdtService {

  Future<AddResourceResponse> addResource(String resourceId, ResourceType resourceType);
}
