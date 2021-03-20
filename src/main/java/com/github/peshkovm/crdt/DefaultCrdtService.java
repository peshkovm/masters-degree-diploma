package com.github.peshkovm.crdt;

import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.routing.fsm.AddResource;
import com.github.peshkovm.raft.Raft;
import io.netty.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DefaultCrdtService implements CrdtService {

  private final Logger logger = LogManager.getLogger();
  private final Raft raft;

  @Autowired
  public DefaultCrdtService(Raft raft) {
    this.raft = raft;
  }

  @Override
  public Future<Boolean> addResource(long resourceId, ResourceType resourceType) {
    raft.command(new AddResource(resourceId, resourceType));
    return null;
  }
}
