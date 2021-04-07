package com.github.peshkovm.common.diagram;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class DiagramMxCellConverter implements Converter {

  @Override
  public Object read(InputNode node) throws Exception {
    return new DiagramMxCellConverter();
  }

  @Override
  public void write(OutputNode node, Object value) throws Exception {
    final int id = DiagramMxCell.getId();
    final int parent = DiagramMxCell.getParent();

    node.setAttribute("id", String.valueOf(id));
    node.setAttribute("parent", String.valueOf(parent));
    node.setName("mxCell");
  }
}
