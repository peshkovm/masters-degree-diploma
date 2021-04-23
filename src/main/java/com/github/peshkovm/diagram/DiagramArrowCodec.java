package com.github.peshkovm.diagram;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.diagram.DiagramFactorySingleton.MessageWithId;
import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.diagram.pojos.ArrowMxCell.ArrowEdgeShape;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.util.List;

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
    if (!diagramHelper.isShouldDiagramContainText()) return "";

    return message.toString();
  }
}
