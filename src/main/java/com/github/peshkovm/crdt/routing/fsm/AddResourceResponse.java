package com.github.peshkovm.crdt.routing.fsm;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.crdt.routing.ResourceType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class AddResourceResponse implements Message {

  private final ResourceType resourceType;
  private final String resourceId;

  public AddResourceResponse(String resourceId, ResourceType resourceType) {
    this.resourceId = resourceId;
    this.resourceType = resourceType;
  }
}
