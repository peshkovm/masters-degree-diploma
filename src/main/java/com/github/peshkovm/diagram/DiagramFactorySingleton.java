package com.github.peshkovm.diagram;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.diagram.pojos.ArrowMxCell.ArrowEdgeShape;
import com.github.peshkovm.diagram.pojos.NodeMxCell;
import com.github.peshkovm.diagram.pojos.SourceMxPoint;
import com.github.peshkovm.diagram.pojos.TargetMxPoint;
import com.github.peshkovm.node.InternalNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Data
public class DiagramFactorySingleton {
  private static volatile DiagramFactorySingleton instance;
  private static volatile DiagramBuilderSingleton diagramBuilder;
  private List<NodeMxCell> nodes;
  private final Map<String, NodeMxCell> nodesMap;
  private final Map<Long, ArrowSourceInfo> arrowsSourceMap;
  private final Map<Long, ArrowTargetInfo> arrowsTargetMap;
  private long id;

  @Value("${diagram.isActive}")
  @Getter
  private boolean isActive;

  private DiagramFactorySingleton() {
    nodesMap = new HashMap<>();
    arrowsSourceMap = new HashMap<>();
    arrowsTargetMap = new HashMap<>();
    id = 0;
  }

  public static DiagramFactorySingleton getInstance() {
    if (instance != null) {
      return instance;
    }
    synchronized (DiagramFactorySingleton.class) {
      if (instance == null) {
        instance = new DiagramFactorySingleton();
      }
      return instance;
    }
  }

  public synchronized void createDiagram(String diagramName, int nodeHeight) {
    diagramBuilder = DiagramBuilderSingleton.getInstance(diagramName, nodeHeight);
    nodes = Collections.unmodifiableList(diagramBuilder.getNodes());
  }

  public synchronized void addNode(InternalNode internalNode, DrawIOColor color) {
    if (!isActive) return;

    final DiagramNodeMeta nodeMeta = internalNode.getBeanFactory().getBean(DiagramNodeMeta.class);

    if (nodes.isEmpty()) diagramBuilder.addNode(nodeMeta.getNodeName(), 40, 80, color);
    else
      diagramBuilder.addNode(
          nodeMeta.getNodeName(),
          nodes.get(nodes.size() - 1).getMxGeometry().getX() + 160,
          80,
          color);
    nodesMap.put(nodes.get(nodes.size() - 1).getValue(), nodes.get(nodes.size() - 1));
  }

  public synchronized MessageWithId wrapMessage(Message message) {
    return new MessageWithId(message, ++id);
  }

  public synchronized void addArrowSourcePoint(
      long id, ArrowEdgeShape startArrowShape, String nodeName, long y) {
    if (!isActive) return;
    if (nodes.isEmpty()) {
      throw new IllegalStateException("Should create at least 2 nodes first");
    }

    final NodeMxCell node = nodesMap.get(nodeName);
    final long x = node.getMxGeometry().getX();

    arrowsSourceMap.put(id, new ArrowSourceInfo(startArrowShape, new SourceMxPoint(x + 40, y)));
  }

  public synchronized void addArrowTargetPoint(
      long id, ArrowEdgeShape endArrowShape, String nodeName, long y) {
    if (!isActive) return;

    if (nodes.isEmpty()) {
      throw new IllegalStateException("Should create at least 2 nodes first");
    }

    final NodeMxCell node = nodesMap.get(nodeName);
    final long x = node.getMxGeometry().getX();

    arrowsTargetMap.put(id, new ArrowTargetInfo(endArrowShape, new TargetMxPoint(x + 40, y)));
  }

  public synchronized void commitArrow(long id, String arrowName, DrawIOColor arrowColor) {
    if (!isActive) return;

    if (nodes.isEmpty()) {
      throw new IllegalStateException("Should create at least 2 nodes first");
    }

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

  public synchronized void buildDiagram() {
    try {
      diagramBuilder.build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized void setOutputPath(String outputPath) {
    diagramBuilder.setOutputFilePath(outputPath);
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
