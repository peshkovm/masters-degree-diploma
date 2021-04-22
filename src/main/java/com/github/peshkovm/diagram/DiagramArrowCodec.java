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
    isActive = diagramHelper.isActive();
    nodeName = diagramNodeMeta.getNodeName();
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
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
  protected void decode(ChannelHandlerContext ctx, MessageWithId msg, List<Object> out)
      throws Exception {
    if (!isActive) {
      out.add(msg.getOriginalMessage());
      return;
    }

    diagramHelper.addArrowTargetPoint(
        msg.getId(), ArrowEdgeShape.CLASSIC, nodeName, System.nanoTime());
    diagramHelper.commitArrow(msg.getId(), msg.getOriginalMessage().toString(), DrawIOColor.GREY);

    out.add(msg.getOriginalMessage());
  }
}
