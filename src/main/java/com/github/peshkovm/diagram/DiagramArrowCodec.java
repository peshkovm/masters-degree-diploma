package com.github.peshkovm.diagram;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.crdt.operationbased.protocol.DownstreamUpdate;
import com.github.peshkovm.crdt.routing.fsm.AddResource;
import com.github.peshkovm.crdt.statebased.protocol.Payload;
import com.github.peshkovm.diagram.DiagramFactorySingleton.MessageWithId;
import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.diagram.pojos.ArrowMxCell.ArrowEdgeShape;
import com.github.peshkovm.raft.protocol.ClientMessage;
import com.github.peshkovm.raft.protocol.ClientMessageSuccessful;
import com.github.peshkovm.raft.protocol.CommandResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.util.List;
import lombok.Getter;

public class DiagramArrowCodec extends MessageToMessageCodec<MessageWithId, Message> {

  private final DiagramFactorySingleton diagramHelper;
  private final String nodeName;
  private final boolean isActive;

  public DiagramArrowCodec(DiagramFactorySingleton diagramHelper, DiagramNodeMeta diagramNodeMeta) {
    this.diagramHelper = diagramHelper;
    isActive = diagramHelper.isDiagramActive();
    nodeName = diagramNodeMeta.getNodeName();
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) {
    if (!isActive) {
      out.add(new MessageWithId(msg, 0L));
      return;
    }

    final MessageWithId messageWithId = diagramHelper.wrapMessage(msg);
    diagramHelper.addArrowSourcePoint(
        messageWithId.getId(), ArrowEdgeShape.OVAL, nodeName, System.nanoTime());

    out.add(messageWithId);
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, MessageWithId msg, List<Object> out) {
    if (!isActive) {
      out.add(msg.getOriginalMessage());
      return;
    }

    diagramHelper.addArrowTargetPoint(
        msg.getId(), ArrowEdgeShape.CLASSIC, nodeName, System.nanoTime());

    String arrowName = getArrowName(msg.getOriginalMessage());

    diagramHelper.commitArrow(msg.getId(), arrowName, DrawIOColor.GREY);

    out.add(msg.getOriginalMessage());
  }

  private String getArrowName(Message message) {
    if (!diagramHelper.isDiagramContainsText()) return "";

    MessageType messageType;
    Message msg;

    if (message instanceof ClientMessage) {
      msg = ((ClientMessage) message).getMessage().getCommand();
      messageType = MessageType.getMessageType(msg.getClass());
    } else if (message instanceof ClientMessageSuccessful) {
      msg = ((ClientMessageSuccessful) message).getCommandResult();
      messageType = MessageType.getMessageType(msg.getClass());
    } else {
      msg = message;
      messageType = MessageType.getMessageType(message.getClass());
    }

    switch (messageType) {
      case ADD_RESOURCE:
        {
          final AddResource addResource = (AddResource) msg;
          return "new " + addResource.getResourceType() + " " + addResource.getResourceId();
        }
      case COMMAND_RESULT:
        {
          final CommandResult commandResult = (CommandResult) msg;
          return commandResult.isSuccessful() ? "successful" : "failure";
        }
      case DOWNSTREAM_UPDATE:
        {
          final DownstreamUpdate<?, ?> downstreamUpdate = (DownstreamUpdate) msg;
          return downstreamUpdate.getAtSourceResult() + ", " + downstreamUpdate.getArgument();
        }
      case PAYLOAD:
        {
          final Payload<?, ?, ?> payload = (Payload) msg;
          return payload.getPayload().toString();
        }
    }

    return message.toString();
  }

  private enum MessageType {
    ADD_RESOURCE(AddResource.class),
    COMMAND_RESULT(CommandResult.class),
    DOWNSTREAM_UPDATE(DownstreamUpdate.class),
    PAYLOAD(Payload.class);

    @Getter private final Class<? extends Message> messageClass;

    MessageType(Class<? extends Message> messageClass) {
      this.messageClass = messageClass;
    }

    static MessageType getMessageType(Class<? extends Message> messageClass) {
      for (MessageType messageType : values()) {
        if (messageClass.equals(messageType.getMessageClass())) return messageType;
      }

      throw new RuntimeException("Unknown message type");
    }
  }
}
