package com.github.peshkovm.crdt.routing.fsm;

import com.github.peshkovm.crdt.routing.ResourceType;
import lombok.Data;

@Data
public class Resource {

  private final String resourceId;
  private final ResourceType resourceType;

  public Resource(String resourceId, ResourceType resourceType) {

    this.resourceId = resourceId;
    this.resourceType = resourceType;
  }
}
