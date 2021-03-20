package com.github.peshkovm.crdt;

import com.github.peshkovm.crdt.routing.ResourceType;
import io.netty.util.concurrent.Future;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

public interface CrdtService {

  Future<Boolean> addResource(long resourceId, ResourceType resourceType);
}
