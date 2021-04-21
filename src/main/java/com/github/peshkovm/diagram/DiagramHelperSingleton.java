package com.github.peshkovm.diagram;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.diagram.pojos.ArrowMxCell;
import com.github.peshkovm.diagram.pojos.ArrowMxCell.ArrowEdgeShape;
import com.github.peshkovm.diagram.pojos.NodeMxCell;
import com.github.peshkovm.diagram.pojos.SourceMxPoint;
import com.github.peshkovm.diagram.pojos.TargetMxPoint;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class DiagramHelperSingleton {
  private static volatile DiagramHelperSingleton instance;
  private static volatile DiagramBuilderSingleton diagramBuilder =
      DiagramBuilderSingleton.getInstance();
  private final List<NodeMxCell> nodes;
  private final List<ArrowMxCell> arrows;
  private final Map<String, NodeMxCell> nodesMap;
  private final Map<Long, ArrowSourceInfo> arrowsSourceMap;
  private final Map<Long, ArrowTargetInfo> arrowsTargetMap;
  private long id;

  private DiagramHelperSingleton() {
    nodes = Collections.unmodifiableList(diagramBuilder.getNodes());
    arrows = Collections.unmodifiableList(diagramBuilder.getArrows());
    nodesMap = new HashMap<>();
    arrowsSourceMap = new HashMap<>();
    arrowsTargetMap = new HashMap<>();
    id = 0;
  }

  public static DiagramHelperSingleton getInstance() {
    if (instance != null) {
      return instance;
    }
    synchronized (DiagramHelperSingleton.class) {
      if (instance == null) {
        instance = new DiagramHelperSingleton();
      }
      return instance;
    }
  }

  public synchronized void addNode(String name, DrawIOColor color) {
    diagramBuilder.addNode(name, color);
    nodesMap.put(nodes.get(nodes.size() - 1).getValue(), nodes.get(nodes.size() - 1));
  }

  public synchronized MessageWithId wrapMessage(Message message) {
    return new MessageWithId(message, ++id);
  }

  public synchronized void addArrowSourcePoint(
      long id, ArrowEdgeShape startArrowShape, String nodeName, long y) {
    final NodeMxCell node = nodesMap.get(nodeName);
    final long x = node.getMxGeometry().getX();

    arrowsSourceMap.put(id, new ArrowSourceInfo(startArrowShape, new SourceMxPoint(x, y)));
  }

  public synchronized void addArrowTargetPoint(
      long id, ArrowEdgeShape endArrowShape, String nodeName, long y) {
    final NodeMxCell node = nodesMap.get(nodeName);
    final long x = node.getMxGeometry().getX();

    arrowsTargetMap.put(id, new ArrowTargetInfo(endArrowShape, new TargetMxPoint(x, y)));
  }

  public synchronized void commitArrow(long id, String arrowName, DrawIOColor arrowColor) {
    final ArrowSourceInfo arrowSourceInfo = arrowsSourceMap.get(id);
    final ArrowTargetInfo arrowTargetInfo = arrowsTargetMap.get(id);

    diagramBuilder.addArrow(
        arrowName,
        arrowColor,
        arrowSourceInfo.startArrow,
        arrowTargetInfo.endArrow,
        arrowSourceInfo.sourceMxPoint,
        arrowTargetInfo.targetMxPoint);
  }

  @Data
  public static class ArrowTargetInfo {
    private final ArrowEdgeShape endArrow;
    private final TargetMxPoint targetMxPoint;

    public ArrowTargetInfo(ArrowEdgeShape endArrow, TargetMxPoint targetMxPoint) {
      this.endArrow = endArrow;
      this.targetMxPoint = targetMxPoint;
    }
  }

  @Data
  private static class ArrowSourceInfo {
    private final ArrowEdgeShape startArrow;
    private final SourceMxPoint sourceMxPoint;

    public ArrowSourceInfo(ArrowEdgeShape startArrow, SourceMxPoint sourceMxPoint) {
      this.startArrow = startArrow;
      this.sourceMxPoint = sourceMxPoint;
    }
  }

  @Data
  public static class MessageWithId implements Message {
    private final Message originalMessage;
    private final long id;

    public MessageWithId(Message originalMessage, long id) {
      this.originalMessage = originalMessage;
      this.id = id;
    }
  }
}
