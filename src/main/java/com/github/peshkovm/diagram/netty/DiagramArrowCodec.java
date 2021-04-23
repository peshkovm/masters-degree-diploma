package com.github.peshkovm.diagram.netty;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.crdt.operationbased.protocol.DownstreamUpdate;
import com.github.peshkovm.crdt.routing.fsm.AddResource;
import com.github.peshkovm.crdt.statebased.protocol.Payload;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.DiagramFactorySingleton.MessageWithId;
import com.github.peshkovm.diagram.MessageType;
import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.diagram.discovery.ClusterDiagramNodeDiscovery;
import com.github.peshkovm.diagram.pojos.ArrowMxCell.ArrowEdgeShape;
import com.github.peshkovm.raft.protocol.ClientMessage;
import com.github.peshkovm.raft.protocol.ClientMessageSuccessful;
import com.github.peshkovm.raft.protocol.CommandResult;
import com.github.peshkovm.transport.DiscoveryNode;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.vavr.collection.HashSet;
import java.util.List;

@Sharable
public class DiagramArrowCodec extends MessageToMessageCodec<MessageWithId, Message> {

  private final DiagramFactorySingleton diagramHelper;
  private final String nodeName;
  private final boolean isActive;
  private final ClusterDiagramNodeDiscovery nodeDiscovery;

  public DiagramArrowCodec(
      DiagramFactorySingleton diagramHelper, ClusterDiagramNodeDiscovery nodeDiscovery) {
    this.diagramHelper = diagramHelper;
    this.nodeDiscovery = nodeDiscovery;
    isActive = diagramHelper.isDiagramActive();
    nodeName = nodeDiscovery.getSelf().getNodeName();
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) {
    if (!isActive || shouldSkipMessage(msg)) {
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
    if (!isActive || shouldSkipMessage(msg.getOriginalMessage())) {
      out.add(msg.getOriginalMessage());
      return;
    }

    diagramHelper.addArrowTargetPoint(
        msg.getId(), ArrowEdgeShape.CLASSIC, nodeName, System.nanoTime());

    String arrowName = getArrowName(msg.getOriginalMessage());

    diagramHelper.commitArrow(msg.getId(), arrowName, DrawIOColor.GREY);

    out.add(msg.getOriginalMessage());
  }

  public void onException(Throwable cause, DiscoveryNode discoveryNode, Message msg) {
    if (!isActive || shouldSkipMessage(msg) || !diagramHelper.isDrawOnError()) return;

    final MessageWithId messageWithId = diagramHelper.wrapMessage(msg);
    diagramHelper.addArrowSourcePoint(
        messageWithId.getId(), ArrowEdgeShape.OVAL, nodeName, System.nanoTime());

    final String targetNodeName =
        nodeDiscovery.getReplicas().get(discoveryNode).get().getNodeName();

    diagramHelper.addArrowTargetPoint(
        messageWithId.getId(), ArrowEdgeShape.CROSS, targetNodeName, System.nanoTime());

    String arrowName = getArrowName(msg);
    diagramHelper.commitArrow(messageWithId.getId(), arrowName, DrawIOColor.RED);
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

  private boolean shouldSkipMessage(Message message) {
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

    return diagramHelper.getMsgsToSkip().contains(messageType)
        || !HashSet.of(MessageType.values()).contains(messageType);
  }
}
