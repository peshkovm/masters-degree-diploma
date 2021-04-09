package com.github.peshkovm.common.diagram;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Data;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.springframework.beans.factory.annotation.Value;

@Data
public class DiagramBuilderSingleton {

  private static volatile DiagramBuilderSingleton instance;
  private String diagramName;
  private String outputFilePath;
  private String outputFileName;
  private final MxFilePojo mxFile;
  private final RootPojo root;
  private final Serializer serializer;
  private Map<NodeMessagePair, MxCellPojo> messageArrowMap;
  private boolean isActive;

  @Value("${diagram.shouldContainText}")
  private boolean shouldContainText;

  @Value("${diagram.nodeHeight}")
  private int nodeHeight;

  private DiagramBuilderSingleton(String diagramName, String outputFilePath, String outputFileName)
      throws Exception {
    this.diagramName = diagramName;
    this.outputFilePath = outputFilePath;
    this.outputFileName = outputFileName;
    root = new RootPojo(new RootMxCell(), new DiagramMxCell(), new ArrayList<>());
    final MxGraphModelPojo mxGraphModel = new MxGraphModelPojo(root);
    final DiagramPojo diagram = new DiagramPojo(diagramName, mxGraphModel);

    mxFile = new MxFilePojo(false, diagram);
    serializer = bindConverters();
    messageArrowMap = new ConcurrentHashMap<>();
  }

  public static DiagramBuilderSingleton getInstance() throws Exception {
    if (instance != null) {
      return instance;
    }
    synchronized (DiagramBuilderSingleton.class) {
      if (instance == null) {
        instance =
            new DiagramBuilderSingleton(
                "Untitled", "src/test/resources/diagram/cluster-crdt-test/", "untitled.xml");
      }
      return instance;
    }
  }

  public synchronized DiagramBuilderSingleton addNode(String nodeName, DrawIOColor color) {
    if (isActive) {
      MxCellPojo node;

      final String style =
          "shape=umlLifeline;"
              + "perimeter=lifelinePerimeter;"
              + "fillColor="
              + color.fillColor
              + ";"
              + "strokeColor="
              + color.strokeColor
              + ";strokeWidth=1";

      if (root.getMxCells().isEmpty()) {
        node =
            new MxCellPojo(
                "node",
                2,
                1,
                nodeName,
                style,
                1,
                0,
                new MxGeometryPojo(40, 40, 80, nodeHeight, "geometry", new ArrayList<>()));
      } else {
        final int previousId = root.getMxCells().get(root.getMxCells().size() - 1).getId();
        int previousNodeX =
            root.getMxCells().stream()
                .filter(mxCell -> mxCell.getType().equals("node"))
                .reduce((first, second) -> second)
                .orElseThrow()
                .getMxGeometry()
                .getX();
        node =
            new MxCellPojo(
                "node",
                previousId + 1,
                1,
                nodeName,
                style,
                1,
                0,
                new MxGeometryPojo(
                    previousNodeX + 160, 40, 80, nodeHeight, "geometry", new ArrayList<>()));
      }

      root.getMxCells().add(node);
    }
    return instance;
  }

  public synchronized DiagramBuilderSingleton addArrow(
      DiscoveryNode discoveryNode,
      Message message,
      String arrowName,
      String arrowColor,
      String startArrow,
      String endArrow,
      String sourceNodeName,
      String targetNodeName,
      long sourceY,
      long targetY) {

    if (isActive) {
      if (root.getMxCells().isEmpty()) {
        throw new IllegalStateException("Should create at least 2 nodes first");
      }

      if (!shouldContainText) {
        arrowName = "";
      }

      final int previousId = root.getMxCells().get(root.getMxCells().size() - 1).getId();
      final String style =
          "strokeWidth=1;"
              + "startArrow="
              + startArrow
              + ";"
              + "endArrow="
              + endArrow
              + ";"
              + "startFill=1;"
              + "fillColor="
              + arrowColor
              + ";"
              + "strokeColor="
              + arrowColor
              + ";";

      final MxCellPojo sourceNode =
          root.getMxCells().stream()
              .filter(mxCell -> mxCell.getType().equals("node"))
              .filter(node -> node.getValue().equals(sourceNodeName))
              .findAny()
              .orElseThrow();
      final MxCellPojo targetNode =
          root.getMxCells().stream()
              .filter(mxCell -> mxCell.getType().equals("node"))
              .filter(node -> node.getValue().equals(targetNodeName))
              .findAny()
              .orElseThrow();

      final int sourceX = sourceNode.getMxGeometry().getX();
      final int targetX = targetNode.getMxGeometry().getX();

      final MxCellPojo arrow =
          new MxCellPojo(
              "arrow",
              previousId + 1,
              1,
              arrowName,
              style,
              0,
              1,
              new MxGeometryPojo(
                  0,
                  0,
                  0,
                  0,
                  "geometry",
                  List.of(
                      new MxPoint(sourceX + 40, sourceY, "sourcePoint"),
                      new MxPoint(targetX + 40, targetY, "targetPoint"))));

      root.getMxCells().add(arrow);

      messageArrowMap.put(new NodeMessagePair(discoveryNode, message), arrow);
    }

    return instance;
  }

  public synchronized void build() throws Exception {
    final List<MxCellPojo> arrows =
        root.getMxCells().stream()
            .filter(mxCell -> mxCell.getType().equals("arrow"))
            //            .sorted(
            //                Comparator.comparingLong(
            //                    arrow -> arrow.getMxGeometry().getMxPoints().get(0).getY()))
            .collect(Collectors.toList());

    //    long offset = 0;
    //
    //    for (int i = 0; i < arrows.size(); i++) {
    //      final MxCellPojo arrow = arrows.get(i);
    //
    //      long arrowSourceY = arrow.getMxGeometry().getMxPoints().get(0).getY();
    //      long arrowTargetY = arrow.getMxGeometry().getMxPoints().get(1).getY();
    //
    //      if (i == 0) offset = arrowSourceY - 40;
    //
    //      arrowSourceY -= offset;
    //      arrowTargetY -= offset;
    //
    //      arrow.getMxGeometry().getMxPoints().get(0).setY(arrowSourceY);
    //      arrow.getMxGeometry().getMxPoints().get(1).setY(arrowTargetY);
    //    }

    final List<Long> sourceYs =
        arrows.stream()
            .map(arrow -> arrow.getMxGeometry().getMxPoints().get(0).getY())
            .collect(Collectors.toList());

    final List<Long> targetYs =
        arrows.stream()
            .map(arrow -> arrow.getMxGeometry().getMxPoints().get(1).getY())
            .collect(Collectors.toList());

    long minArrowSourceY = Collections.min(sourceYs);
    long maxArrowTargetY = Collections.max(targetYs);

    for (int i = 0; i < arrows.size(); i++) {
      final MxCellPojo arrow = arrows.get(i);

      long arrowSourceY = arrow.getMxGeometry().getMxPoints().get(0).getY();
      long arrowTargetY = arrow.getMxGeometry().getMxPoints().get(1).getY();

      arrowSourceY = fitNumberInRange(arrowSourceY, minArrowSourceY, maxArrowTargetY);
      arrowTargetY = fitNumberInRange(arrowTargetY, minArrowSourceY, maxArrowTargetY);

      arrow.getMxGeometry().getMxPoints().get(0).setY(arrowSourceY);
      arrow.getMxGeometry().getMxPoints().get(1).setY(arrowTargetY);
    }

    File result = new File(outputFilePath + outputFileName);

    serializer.write(mxFile, result);
  }

  public void setDiagramName(String diagramName) {
    this.diagramName = diagramName;
    mxFile.getDiagram().setName(diagramName);
  }

  private static Serializer bindConverters() throws Exception {
    final Serializer serializer;
    Registry registry = new Registry();
    Strategy strategy = new RegistryStrategy(registry);
    serializer = new Persister(strategy);

    registry.bind(RootMxCell.class, RootMxCellConverter.class);
    registry.bind(DiagramMxCell.class, DiagramMxCellConverter.class);
    return serializer;
  }

  private long fitNumberInRange(long x, long min, long max) {
    final int b = nodeHeight;
    final int a = 120;

    return ((b - a) * (x - min)) / (max - min) + a;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  public void removeArrow(NodeMessagePair nodeMessagePair) {
    final MxCellPojo arrow = messageArrowMap.remove(nodeMessagePair);

    root.getMxCells().remove(arrow);
  }

  public void setEndArrow(NodeMessagePair nodeMessagePair, String endArrow) {
    final MxCellPojo arrow = messageArrowMap.get(nodeMessagePair);

    final String style = root.getMxCells().get(root.getMxCells().indexOf(arrow)).getStyle();

    final String changedStyle = style.replace("classic", endArrow);

    root.getMxCells().get(root.getMxCells().indexOf(arrow)).setStyle(changedStyle);
    messageArrowMap.get(nodeMessagePair).setStyle(changedStyle);
  }

  public void setArrowColor(NodeMessagePair nodeMessagePair, String arrowColor) {
    final MxCellPojo arrow = messageArrowMap.get(nodeMessagePair);

    final String style = root.getMxCells().get(root.getMxCells().indexOf(arrow)).getStyle();

    final String changedStyle = style.replace(DrawIOColor.GREY.strokeColor, arrowColor);

    root.getMxCells().get(root.getMxCells().indexOf(arrow)).setStyle(changedStyle);
    messageArrowMap.get(nodeMessagePair).setStyle(changedStyle);
  }
}
