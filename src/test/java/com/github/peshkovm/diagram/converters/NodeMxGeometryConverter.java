package com.github.peshkovm.diagram.converters;

import com.github.peshkovm.diagram.pojos.NodeMxGeometry;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class NodeMxGeometryConverter implements Converter<NodeMxGeometry> {

  @Override
  public NodeMxGeometry read(InputNode node) throws Exception {
    throw new UnsupportedOperationException("Deserialization is not supported");
  }

  @Override
  public void write(OutputNode outputNode, NodeMxGeometry nodeMxGeometry) throws Exception {
    final int x = nodeMxGeometry.getX();
    final int y = nodeMxGeometry.getY();
    final int width = nodeMxGeometry.getWidth();
    final int height = nodeMxGeometry.getHeight();
    final String as = nodeMxGeometry.getAs();

    outputNode.setAttribute("x", String.valueOf(x));
    outputNode.setAttribute("y", String.valueOf(y));
    outputNode.setAttribute("width", String.valueOf(width));
    outputNode.setAttribute("height", String.valueOf(height));
    outputNode.setAttribute("as", String.valueOf(as));
    outputNode.setName("mxGeometry");
  }
}
