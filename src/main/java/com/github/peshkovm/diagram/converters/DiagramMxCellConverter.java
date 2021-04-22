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
  public void write(OutputNode outputNode, DiagramMxCell diagramMxCell) throws Exception {
    final int id = DiagramMxCell.getId();
    final int parent = DiagramMxCell.getParent();

    outputNode.setAttribute("id", String.valueOf(id));
    outputNode.setAttribute("parent", String.valueOf(parent));
    outputNode.setName("mxCell");
  }
}
