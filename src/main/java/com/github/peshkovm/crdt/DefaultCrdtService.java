package com.github.peshkovm.crdt;

import com.github.peshkovm.crdt.registry.CrdtRegistry;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.routing.fsm.AddResource;
import com.github.peshkovm.crdt.routing.fsm.AddResourceResponse;
import com.github.peshkovm.crdt.routing.fsm.Resource;
import com.github.peshkovm.raft.Raft;
import io.vavr.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DefaultCrdtService implements CrdtService {

  private final Logger logger = LogManager.getLogger();
  private final Raft raft;
  private final CrdtRegistry registry;

  @Autowired
  public DefaultCrdtService(Raft raft, Sinks.Many<Resource> eventBus, CrdtRegistry registry) {
    this.raft = raft;
    eventBus.asFlux().subscribe(this::handle);
    this.registry = registry;
  }

  @Override
  public Future<AddResourceResponse> addResource(String resourceId, ResourceType resourceType) {
    return raft.command(new AddResource(resourceId, resourceType))
        .filter(m -> m instanceof AddResourceResponse)
        .map(m -> ((AddResourceResponse) m));
  }

  private void processReplica(Resource resource) {
    switch (resource.getResourceType()) {
      case GCounter:
        final boolean result = registry.createGCounter(resource.getResourceId());
        if (result) {
          logger.info("Successfully created GCounter");
        } else {
          logger.error("GCounter with id: {} already exists", resource::getResourceId);
        }
        break;
      default:
        logger.warn("Unexpected crdt type: {}", resource.getResourceType());
    }
  }

  private void handle(Resource resource) {
    processReplica(resource);
  }
}
