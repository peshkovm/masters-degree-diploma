package com.github.peshkovm.diagram.converters;

import com.github.peshkovm.diagram.pojos.ArrowMxCell;
import com.github.peshkovm.diagram.pojos.ArrowMxCell.ArrowEdgeShape;
import com.github.peshkovm.diagram.pojos.ArrowMxGeometry;
import com.github.peshkovm.diagram.pojos.SourceMxPoint;
import com.github.peshkovm.diagram.pojos.TargetMxPoint;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class ArrowMxCellConverter implements Converter<ArrowMxCell> {
  private OutputNode arrowMxCellNode;
  private ArrowMxCell arrowMxCell;
  private ArrowMxGeometry mxGeometry;
  private OutputNode mxGeometryNode;
  private SourceMxPoint sourceMxPoint;
  private OutputNode sourceMxPointNode;
  private TargetMxPoint targetMxPoint;
  private OutputNode targetMxPointNode;

  @Override
  public ArrowMxCell read(InputNode node) throws Exception {
    throw new UnsupportedOperationException("Deserialization is not supported");
  }

  @Override
  public void write(OutputNode outputNode, ArrowMxCell arrowMxCell) throws Exception {
    this.arrowMxCell = arrowMxCell;
    this.arrowMxCellNode = outputNode;

    convertArrowMxCell();
    convertMxGeometry();
    convertSourceMxPoint();
    convertTargetMxPoint();

    arrowMxCellNode.commit();
  }

  private void convertArrowMxCell() {
    final int id = this.arrowMxCell.getId();
    final int parent = this.arrowMxCell.getParent();
    final String value = this.arrowMxCell.getValue();
    final String style = this.arrowMxCell.getStyle();
    final int edge = this.arrowMxCell.getEdge();
    final ArrowEdgeShape startArrow = this.arrowMxCell.getStartArrow();
    final ArrowEdgeShape endArrow = this.arrowMxCell.getEndArrow();

    arrowMxCellNode.setAttribute("id", String.valueOf(id));
    arrowMxCellNode.setAttribute("parent", String.valueOf(parent));
    arrowMxCellNode.setAttribute("value", String.valueOf(value));
    arrowMxCellNode.setAttribute("style", String.valueOf(style));
    arrowMxCellNode.setAttribute("edge", String.valueOf(edge));
    arrowMxCellNode.setAttribute("startArrow", String.valueOf(startArrow));
    arrowMxCellNode.setAttribute("endArrow", String.valueOf(endArrow));
    arrowMxCellNode.setName("mxCell");
  }

  private void convertMxGeometry() throws Exception {
    mxGeometry = this.arrowMxCell.getMxGeometry();
    mxGeometryNode = arrowMxCellNode.getChild("arrowMxGeometry");
    final String as = mxGeometry.getAs();

    mxGeometryNode.setAttribute("as", as);
    mxGeometryNode.setName("mxGeometry");
  }

  private void convertSourceMxPoint() throws Exception {
    sourceMxPointNode = mxGeometryNode.getChild("sourceMxPoint");
    sourceMxPoint = mxGeometry.getSourceMxPoint();
    final String as = sourceMxPoint.getAs();
    final long x = sourceMxPoint.getX();
    final long y = sourceMxPoint.getY();

    sourceMxPointNode.setAttribute("as", as);
    sourceMxPointNode.setAttribute("x", String.valueOf(x));
    sourceMxPointNode.setAttribute("y", String.valueOf(y));
    sourceMxPointNode.setName("mxPoint");
  }

  private void convertTargetMxPoint() throws Exception {
    targetMxPointNode = mxGeometryNode.getChild("targetMxPoint");
    targetMxPoint = mxGeometry.getTargetMxPoint();
    final String as = targetMxPoint.getAs();
    final long x = targetMxPoint.getX();
    final long y = targetMxPoint.getY();

    targetMxPointNode.setAttribute("as", as);
    targetMxPointNode.setAttribute("x", String.valueOf(x));
    targetMxPointNode.setAttribute("y", String.valueOf(y));
    targetMxPointNode.setName("mxPoint");
  }
}
