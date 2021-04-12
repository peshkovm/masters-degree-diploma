package com.github.peshkovm.diagram.converters;

import com.github.peshkovm.diagram.pojos.NodeMxCell;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class NodeMxCellConverter implements Converter<NodeMxCell> {

  @Override
  public NodeMxCell read(InputNode node) throws Exception {
    throw new UnsupportedOperationException("Deserialization is not supported");
  }

  @Override
  public void write(OutputNode outputNode, NodeMxCell nodeMxCell) throws Exception {
    final int id = nodeMxCell.getId();
    final int parent = nodeMxCell.getParent();
    final String value = nodeMxCell.getValue();
    final String style = nodeMxCell.getStyle();
    final int vertex = nodeMxCell.getVertex();

    outputNode.setAttribute("id", String.valueOf(id));
    outputNode.setAttribute("parent", String.valueOf(parent));
    outputNode.setAttribute("value", String.valueOf(value));
    outputNode.setAttribute("style", String.valueOf(style));
    outputNode.setAttribute("vertex", String.valueOf(vertex));
    outputNode.setName("mxCell");
  }
}
