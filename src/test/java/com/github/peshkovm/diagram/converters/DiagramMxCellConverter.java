package com.github.peshkovm.diagram.converters;

import com.github.peshkovm.diagram.pojos.DiagramMxCell;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class DiagramMxCellConverter implements Converter<DiagramMxCell> {

  @Override
  public DiagramMxCell read(InputNode node) throws Exception {
    throw new UnsupportedOperationException("Deserialization is not supported");
  }

  @Override
  public void write(OutputNode node, DiagramMxCell diagramMxCell) throws Exception {
    final int id = diagramMxCell.getId();
    final int parent = diagramMxCell.getParent();

    node.setAttribute("id", String.valueOf(id));
    node.setAttribute("parent", String.valueOf(parent));
    node.setName("mxCell");
  }
}
