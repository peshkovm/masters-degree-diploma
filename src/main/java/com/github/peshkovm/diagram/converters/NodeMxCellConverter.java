package com.github.peshkovm.diagram.converters;

import com.github.peshkovm.diagram.pojos.NodeMxCell;
import com.github.peshkovm.diagram.pojos.NodeMxGeometry;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class NodeMxCellConverter implements Converter<NodeMxCell> {

  private OutputNode nodeMxCellNode;
  private NodeMxCell nodeMxCell;

  @Override
  public NodeMxCell read(InputNode node) throws Exception {
    throw new UnsupportedOperationException("Deserialization is not supported");
  }

  @Override
  public void write(OutputNode outputNode, NodeMxCell nodeMxCell) throws Exception {
    nodeMxCellNode = outputNode;
    this.nodeMxCell = nodeMxCell;

    convertNodeMxCell();
    convertMxGeometry();

    outputNode.commit();
  }

  private void convertMxGeometry() throws Exception {
    final NodeMxGeometry mxGeometry = this.nodeMxCell.getMxGeometry();
    final OutputNode mxGeometryNode = nodeMxCellNode.getChild("mxGeometry");
    final long x = mxGeometry.getX();
    final long y = mxGeometry.getY();
    final int width = mxGeometry.getWidth();
    final int height = mxGeometry.getHeight();
    final String as = mxGeometry.getAs();

    mxGeometryNode.setAttribute("x", String.valueOf(x));
    mxGeometryNode.setAttribute("y", String.valueOf(y));
    mxGeometryNode.setAttribute("width", String.valueOf(width));
    mxGeometryNode.setAttribute("height", String.valueOf(height));
    mxGeometryNode.setAttribute("as", String.valueOf(as));
    mxGeometryNode.setName("mxGeometry");
  }

  private void convertNodeMxCell() {
    final int id = nodeMxCell.getId();
    final int parent = nodeMxCell.getParent();
    final String value = nodeMxCell.getValue();
    final String style = nodeMxCell.getStyle();
    final int vertex = nodeMxCell.getVertex();

    nodeMxCellNode.setAttribute("id", String.valueOf(id));
    nodeMxCellNode.setAttribute("parent", String.valueOf(parent));
    nodeMxCellNode.setAttribute("value", String.valueOf(value));
    nodeMxCellNode.setAttribute("style", String.valueOf(style));
    nodeMxCellNode.setAttribute("vertex", String.valueOf(vertex));
    nodeMxCellNode.setName("mxCell");
  }
}
