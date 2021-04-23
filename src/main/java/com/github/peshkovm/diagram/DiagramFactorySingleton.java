package com.github.peshkovm.diagram;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.diagram.discovery.ClusterDiagramNodeDiscovery;
import com.github.peshkovm.diagram.pojos.ArrowMxCell.ArrowEdgeShape;
import com.github.peshkovm.diagram.pojos.NodeMxCell;
import com.github.peshkovm.diagram.pojos.SourceMxPoint;
import com.github.peshkovm.diagram.pojos.TargetMxPoint;
import com.github.peshkovm.node.InternalNode;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class DiagramFactorySingleton {
  private static volatile DiagramFactorySingleton instance;
  private static volatile DiagramBuilderSingleton diagramBuilder;
  private List<NodeMxCell> nodes;
  private final Map<String, NodeMxCell> nodesMap;
  private final Map<Long, ArrowSourceInfo> arrowsSourceMap;
  private final Map<Long, ArrowTargetInfo> arrowsTargetMap;
  private Set<MessageType> msgsToSkip;
  private boolean isDrawOnError;
  private long id;

  //  @Value("${diagram.isActive}")
  @Getter private boolean isDiagramActive;

  //  @Value("${diagram.isContainsText}")
  @Getter private boolean isDiagramContainsText;

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

  public synchronized void createDiagram(
      String diagramName,
      int nodeHeight,
      boolean isDiagramActive,
      boolean isDiagramContainsText,
      boolean isDrawOnError,
      MessageType... msgsToSkip) {
    diagramBuilder = DiagramBuilderSingleton.getInstance(diagramName, nodeHeight);
    nodes = Collections.unmodifiableList(diagramBuilder.getNodes());
    this.isDrawOnError = isDrawOnError;
    this.isDiagramActive = isDiagramActive;
    this.isDiagramContainsText = isDiagramContainsText;
    this.msgsToSkip = HashSet.of(msgsToSkip);
    log.info("Skipping {}", this.msgsToSkip.mkString());
  }

  public synchronized void addNode(InternalNode internalNode, DrawIOColor color) {
    final DiagramNodeMeta nodeMeta =
        internalNode.getBeanFactory().getBean(ClusterDiagramNodeDiscovery.class).getSelf();

    if (nodes.isEmpty()) diagramBuilder.addNode(nodeMeta.getNodeName(), 40, 80, color);
    else
      diagramBuilder.addNode(
          nodeMeta.getNodeName(),
          nodes.get(nodes.size() - 1).getMxGeometry().getX() + 160,
          80,
          color);
    nodesMap.put(nodes.get(nodes.size() - 1).getValue(), nodes.get(nodes.size() - 1));
  }

  public synchronized void addArrowSourcePoint(
      long id, ArrowEdgeShape startArrowShape, String nodeName, long y) {
    if (nodes.isEmpty()) {
      throw new IllegalStateException("Should create at least 2 nodes first");
    }

    final NodeMxCell node = nodesMap.get(nodeName);
    final long x = node.getMxGeometry().getX();

    arrowsSourceMap.put(id, new ArrowSourceInfo(startArrowShape, new SourceMxPoint(x + 40, y)));
  }

  public synchronized void addArrowTargetPoint(
      long id, ArrowEdgeShape endArrowShape, String nodeName, long y) {
    if (nodes.isEmpty()) {
      throw new IllegalStateException("Should create at least 2 nodes first");
    }

    final NodeMxCell node = nodesMap.get(nodeName);
    final long x = node.getMxGeometry().getX();

    arrowsTargetMap.put(id, new ArrowTargetInfo(endArrowShape, new TargetMxPoint(x + 40, y)));
  }

  public synchronized void commitArrow(long id, String arrowName, DrawIOColor arrowColor) {
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

  public boolean isDrawOnError() {
    return isDrawOnError;
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

  public synchronized MessageWithId wrapMessage(Message message) {
    return new MessageWithId(message, ++id);
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
