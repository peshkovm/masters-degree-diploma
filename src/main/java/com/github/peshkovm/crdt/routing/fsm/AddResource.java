package com.github.peshkovm.crdt.routing.fsm;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.crdt.routing.ResourceType;
import lombok.Data;

@Data
public class AddResource implements Message {

  private final String resourceId;
  private final ResourceType resourceType;

  public AddResource(String resourceId, ResourceType resourceType) {
    this.resourceId = resourceId;
    this.resourceType = resourceType;
  }
}
