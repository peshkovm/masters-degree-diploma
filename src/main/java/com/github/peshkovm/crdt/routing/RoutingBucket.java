package com.github.peshkovm.crdt.routing;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.crdt.routing.fsm.Resource;
import io.vavr.collection.Map;
import lombok.Data;

@Data
public class RoutingBucket implements Message {

  private final int index;
  private final Map<Long, RoutingReplica> replicas;
  private final Map<Long, Resource> resources;

  public RoutingBucket(
      int index, Map<Long, RoutingReplica> replicas, Map<Long, Resource> resources) {
    this.index = index;
    this.replicas = replicas;
    this.resources = resources;
  }
}
