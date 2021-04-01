package com.github.peshkovm.crdt;

import com.github.peshkovm.crdt.commutative.protocol.DownstreamUpdate;
import com.github.peshkovm.crdt.registry.CrdtRegistry;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.routing.fsm.AddResource;
import com.github.peshkovm.crdt.routing.fsm.AddResourceResponse;
import com.github.peshkovm.crdt.routing.fsm.Resource;
import com.github.peshkovm.raft.Raft;
import com.github.peshkovm.transport.TransportController;
import io.vavr.concurrent.Future;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

/**
 * Default implementation of {@link CrdtService}.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DefaultCrdtService implements CrdtService {

  private final Logger logger = LogManager.getLogger();
  private final Raft raft;
  private final CrdtRegistry crdtRegistry;

  @Autowired
  public DefaultCrdtService(
      Raft raft,
      Sinks.Many<Resource> eventBus,
      CrdtRegistry crdtRegistry,
      TransportController transportController) {
    this.raft = raft;
    eventBus.asFlux().subscribe(this::handle, eventBus::tryEmitError);
    this.crdtRegistry = crdtRegistry;

    transportController.registerMessageHandler(DownstreamUpdate.class, this::handle);
  }

  @Override
  public Future<Boolean> addResource(String resourceId, ResourceType resourceType) {
    return raft.command(new AddResource(resourceId, resourceType))
        .filter(m -> m instanceof AddResourceResponse)
        .map(m -> ((AddResourceResponse) m).isCreated());
  }

  private void processReplica(Resource resource) {
    switch (resource.getResourceType()) {
      case GCounter:
        final boolean result = crdtRegistry.createGCounter(resource.getResourceId());
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

  @Override
  public CrdtRegistry crdtRegistry() {
    return crdtRegistry;
  }

  private void handle(Resource resource) {
    processReplica(resource);
  }

  private <T extends Serializable, R extends Serializable> void handle(
      DownstreamUpdate<T, R> downstreamUpdate) {
    final String crdtId = downstreamUpdate.getCrdtId();
    final var crdtType = downstreamUpdate.getCrdtType();

    final var crdt = crdtRegistry().crdt(crdtId, crdtType);

    crdt.downstream(downstreamUpdate.getAtSourceResult(), downstreamUpdate.getArgument());
  }
}
