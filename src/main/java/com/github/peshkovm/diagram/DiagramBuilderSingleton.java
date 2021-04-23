package com.github.peshkovm.diagram;

import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.diagram.converters.ArrowMxCellConverter;
import com.github.peshkovm.diagram.converters.DiagramMxCellConverter;
import com.github.peshkovm.diagram.converters.NodeMxCellConverter;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import lombok.Data;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

@Data
public class DiagramBuilderSingleton {

  private static volatile DiagramBuilderSingleton instance;
  private String outputFilePath;
  private String outputFileName;
  private final MxFile mxFile;
  private final List<NodeMxCell> nodes;
  private final List<ArrowMxCell> arrows;
  private final Serializer serializer;
  private int nodeHeight;

  private DiagramBuilderSingleton(String diagramName, int nodeHeight) {
    this.nodeHeight = nodeHeight;

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
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return serializer;
  }

  public static DiagramBuilderSingleton getInstance(String diagramName, int nodeHeight) {
    if (instance != null) {
      return instance;
    }
    synchronized (DiagramBuilderSingleton.class) {
      if (instance == null) {
        instance = new DiagramBuilderSingleton(diagramName, nodeHeight);
      }
      return instance;
    }
  }

  public synchronized DiagramBuilderSingleton addNode(String name, long x, DrawIOColor color) {
    NodeMxCell newNode;
    final int diagramMxCellId = DiagramMxCell.getId();

    newNode =
        new NodeMxCell(
            nodes.size() + 1 + diagramMxCellId,
            name,
            color,
            new NodeMxGeometry(x, 40, 80, nodeHeight));

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

    if (nodes.isEmpty()) {
      throw new IllegalStateException("Should create at least 2 nodes first");
    }

    final int diagramMxCellId = DiagramMxCell.getId();

    final ArrowMxCell newArrow =
        new ArrowMxCell(
            nodes.size() + arrows.size() + 1 + diagramMxCellId,
            name,
            startArrow,
            endArrow,
            color,
            new ArrowMxGeometry(sourceMxPoint, targetMxPoint));

    arrows.add(newArrow);

    return instance;
  }

  public synchronized void build() throws Exception {
    final long minSourceY =
        arrows.stream()
            .map(arrow -> arrow.getMxGeometry().getSourceMxPoint().getY())
            .min(Comparator.comparingLong(sourceY -> sourceY))
            .orElseThrow();

    final long maxTargetY =
        arrows.stream()
            .map(arrow -> arrow.getMxGeometry().getTargetMxPoint().getY())
            .max(Comparator.comparingLong(targetY -> targetY))
            .orElseThrow();

    for (int i = 0; i < arrows.size(); i++) {
      final ArrowMxCell arrow = arrows.get(i);

      final long sourceX = arrow.getMxGeometry().getSourceMxPoint().getX();
      final long sourceY = arrow.getMxGeometry().getSourceMxPoint().getY();
      final long targetX = arrow.getMxGeometry().getTargetMxPoint().getX();
      final long targetY = arrow.getMxGeometry().getTargetMxPoint().getY();

      final long newSourceY = fitNumberInRange(sourceY, minSourceY, maxTargetY);
      final long newTargetY = fitNumberInRange(targetY, minSourceY, maxTargetY);

      arrows.set(
          i,
          new ArrowMxCell(
              arrow.getId(),
              arrow.getValue(),
              arrow.getStartArrow(),
              arrow.getEndArrow(),
              arrow.getColor(),
              new ArrowMxGeometry(
                  new SourceMxPoint(sourceX, newSourceY), new TargetMxPoint(targetX, newTargetY))));
    }

    File outFile = new File(outputFilePath);
    Files.createDirectories(Paths.get(outputFilePath).getParent());

    serializer.write(mxFile, outFile);
  }

  private long fitNumberInRange(long num, long min, long max) {
    final int b = nodeHeight;
    final int a = 120;

    return ((b - a) * (num - min)) / (max - min) + a;
  }
}
