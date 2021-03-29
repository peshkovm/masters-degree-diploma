package com.github.peshkovm.crdt;

import com.github.peshkovm.crdt.routing.ResourceType;
import io.vavr.concurrent.Future;

public interface CrdtService {

  Future<Boolean> addResource(String resourceId, ResourceType resourceType);
}
