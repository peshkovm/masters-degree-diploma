package com.github.peshkovm.diagram;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.crdt.operationbased.protocol.DownstreamUpdate;
import com.github.peshkovm.crdt.routing.fsm.AddResource;
import com.github.peshkovm.crdt.statebased.protocol.Payload;
import com.github.peshkovm.raft.protocol.CommandResult;
import lombok.Getter;

public enum MessageType {
  ADD_RESOURCE(AddResource.class),
  COMMAND_RESULT(CommandResult.class),
  DOWNSTREAM_UPDATE(DownstreamUpdate.class),
  PAYLOAD(Payload.class);

  @Getter private final Class<? extends Message> messageClass;

  MessageType(Class<? extends Message> messageClass) {
    this.messageClass = messageClass;
  }

  public static MessageType getMessageType(Class<? extends Message> messageClass) {
    for (MessageType messageType : values()) {
      if (messageClass.equals(messageType.getMessageClass())) return messageType;
    }

    throw new RuntimeException("Unknown message type");
  }
}
