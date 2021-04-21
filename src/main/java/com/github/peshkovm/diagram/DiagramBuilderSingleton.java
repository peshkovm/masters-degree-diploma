package com.github.peshkovm.diagram;

import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.diagram.converters.ArrowMxCellConverter;
import com.github.peshkovm.diagram.converters.ArrowMxGeometryConverter;
import com.github.peshkovm.diagram.converters.DiagramMxCellConverter;
import com.github.peshkovm.diagram.converters.NodeMxCellConverter;
import com.github.peshkovm.diagram.converters.NodeMxGeometryConverter;
import com.github.peshkovm.diagram.converters.RootMxCellConverter;
import com.github.peshkovm.diagram.pojos.ArrowMxCell;
import com.github.peshkovm.diagram.pojos.ArrowMxCell.ArrowEdgeShape;
import com.github.peshkovm.diagram.pojos.ArrowMxGeometry;
import com.github.peshkovm.diagram.pojos.Diagram;
import com.github.peshkovm.diagram.pojos.DiagramMxCell;
import com.github.peshkovm.diagram.pojos.MxFile;
import com.github.peshkovm.diagram.pojos.MxGraphModel;
import com.github.peshkovm.diagram.pojos.NodeMxCell;
import com.github.peshkovm.diagram.pojos.NodeMxGeometry;
import com.github.peshkovm.diagram.pojos.Root;
import com.github.peshkovm.diagram.pojos.RootMxCell;
import com.github.peshkovm.diagram.pojos.SourceMxPoint;
import com.github.peshkovm.diagram.pojos.TargetMxPoint;
import java.io.File;
import java.util.Comparator;
import java.util.List;
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
  private final MxFile mxFile;
  private final List<NodeMxCell> nodes;
  private final List<ArrowMxCell> arrows;
  private final Serializer serializer;

  @Value("${diagram.shouldContainText}")
  private boolean shouldContainText;

  @Value("${diagram.nodeHeight}")
  private int nodeHeight;

  @Value("${diagram.isActive}")
  private boolean isActive;

  private DiagramBuilderSingleton() {
    Root root = new Root(new RootMxCell(), new DiagramMxCell());
    final MxGraphModel mxGraphModel = new MxGraphModel(root);
    final Diagram diagram = new Diagram(diagramName, mxGraphModel);
    this.mxFile = new MxFile(diagram);
    this.nodes = mxFile.getDiagram().getMxGraphModel().getRoot().getNodes();
    this.arrows = mxFile.getDiagram().getMxGraphModel().getRoot().getArrows();

    serializer = bindConverters();
  }

  private static Serializer bindConverters() {
    final Serializer serializer;
    Registry registry = new Registry();
    Strategy strategy = new RegistryStrategy(registry);
    serializer = new Persister(strategy);

    try {
      registry.bind(RootMxCell.class, RootMxCellConverter.class);
      registry.bind(DiagramMxCell.class, DiagramMxCellConverter.class);
      registry.bind(NodeMxCell.class, NodeMxCellConverter.class);
      registry.bind(ArrowMxCell.class, ArrowMxCellConverter.class);
      registry.bind(NodeMxGeometry.class, NodeMxGeometryConverter.class);
      registry.bind(ArrowMxGeometry.class, ArrowMxGeometryConverter.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return serializer;
  }

  public static DiagramBuilderSingleton getInstance() {
    if (instance != null) {
      return instance;
    }
    synchronized (DiagramBuilderSingleton.class) {
      if (instance == null) {
        instance = new DiagramBuilderSingleton();
      }
      return instance;
    }
  }

  public synchronized DiagramBuilderSingleton addNode(String name, DrawIOColor color) {
    if (!isActive) return instance;

    NodeMxCell newNode;

    newNode = new NodeMxCell(nodes.size(), name, color, new NodeMxGeometry(40, 40, 80, nodeHeight));

    nodes.add(newNode);

    return instance;
  }

  public synchronized DiagramBuilderSingleton addArrow(
      String name,
      DrawIOColor color,
      ArrowEdgeShape startArrow,
      ArrowEdgeShape endArrow,
      SourceMxPoint sourceMxPoint,
      TargetMxPoint targetMxPoint) {

    if (!isActive) return instance;

    if (nodes.isEmpty()) {
      throw new IllegalStateException("Should create at least 2 nodes first");
    }

    final ArrowMxCell newArrow =
        new ArrowMxCell(
            nodes.size() + arrows.size(),
            name,
            startArrow,
            endArrow,
            color,
            new ArrowMxGeometry(sourceMxPoint, targetMxPoint));

    arrows.add(newArrow);

    return instance;
  }

  public synchronized void build() throws Exception {
    if (!isActive) return;

    final long minSourceX =
        arrows.stream()
            .map(arrow -> arrow.getMxGeometry().getSourceMxPoint().getX())
            .min(Comparator.comparingLong(sourceX -> sourceX))
            .orElseThrow();

    final long maxTargetY =
        arrows.stream()
            .map(arrow -> arrow.getMxGeometry().getTargetMxPoint().getY())
            .max(Comparator.comparingLong(sourceX -> sourceX))
            .orElseThrow();

    for (int i = 0; i < arrows.size(); i++) {
      final ArrowMxCell arrow = arrows.get(i);

      final long sourceX = arrow.getMxGeometry().getSourceMxPoint().getX();
      final long sourceY = arrow.getMxGeometry().getSourceMxPoint().getY();
      final long targetX = arrow.getMxGeometry().getTargetMxPoint().getX();
      final long targetY = arrow.getMxGeometry().getTargetMxPoint().getY();

      final long newSourceX = fitNumberInRange(sourceX, minSourceX, maxTargetY);
      final long newSourceY = fitNumberInRange(sourceY, minSourceX, maxTargetY);
      final long newTargetX = fitNumberInRange(targetX, minSourceX, maxTargetY);
      final long newTargetY = fitNumberInRange(targetY, minSourceX, maxTargetY);

      arrows.set(
          i,
          new ArrowMxCell(
              arrow.getId(),
              arrow.getValue(),
              arrow.getStartArrow(),
              arrow.getEndArrow(),
              arrow.getColor(),
              new ArrowMxGeometry(
                  new SourceMxPoint(newSourceX, newSourceY),
                  new TargetMxPoint(newTargetX, newTargetY))));
    }

    File result = new File(outputFilePath + outputFileName);

    serializer.write(mxFile, result);
  }

  private long fitNumberInRange(long num, long min, long max) {
    final int b = nodeHeight;
    final int a = 120;

    return ((b - a) * (num - min)) / (max - min) + a;
  }
}
