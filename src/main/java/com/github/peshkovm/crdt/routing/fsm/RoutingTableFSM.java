package com.github.peshkovm.crdt.routing.fsm;

import com.github.peshkovm.raft.resource.ResourceFSM;
import com.github.peshkovm.raft.resource.ResourceRegistry;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

@Component
public class RoutingTableFSM implements ResourceFSM {

  private final Logger logger = LogManager.getLogger();
  private final Sinks.Many<Resource> eventBus;

  public RoutingTableFSM(ResourceRegistry registry, Sinks.Many<Resource> eventBus) {
    registry.registerHandler(AddResource.class, this::handle);
    this.eventBus = eventBus;
  }

  private AddResourceResponse handle(AddResource addResource) {
    final Resource resource =
        new Resource(addResource.getResourceId(), addResource.getResourceType());

    final EmitResult emitResult = eventBus.tryEmitNext(resource);
    final AtomicBoolean isSuccessful = new AtomicBoolean(true);
    eventBus
        .asFlux()
        .subscribe(
            (Resource resource1) -> {
            },
            throwable -> {
              logger.error("Event Bus caught exception: ", throwable);
              isSuccessful.set(false);
            });

    if (emitResult.isSuccess() && isSuccessful.get()) {
      logger.info("Successfully added resource: {}", () -> resource);
      return new AddResourceResponse(
          addResource.getResourceId(), addResource.getResourceType(), true);
    } else {
      logger.error("Error adding resource: {}", () -> resource);
      return new AddResourceResponse(
          addResource.getResourceId(), addResource.getResourceType(), false);
    }
  }
}
