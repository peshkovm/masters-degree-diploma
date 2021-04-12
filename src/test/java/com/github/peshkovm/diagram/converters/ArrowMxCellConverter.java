package com.github.peshkovm.diagram.converters;

import com.github.peshkovm.diagram.pojos.ArrowMxCell;
import com.github.peshkovm.diagram.pojos.ArrowMxCell.ArrowEdgeShape;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class ArrowMxCellConverter implements Converter<ArrowMxCell> {

  @Override
  public ArrowMxCell read(InputNode node) throws Exception {
    throw new UnsupportedOperationException("Deserialization is not supported");
  }

  @Override
  public void write(OutputNode outputNode, ArrowMxCell arrowMxCell) throws Exception {
    final int id = arrowMxCell.getId();
    final int parent = arrowMxCell.getParent();
    final String value = arrowMxCell.getValue();
    final String style = arrowMxCell.getStyle();
    final int edge = arrowMxCell.getEdge();
    final ArrowEdgeShape startArrow = arrowMxCell.getStartArrow();
    final ArrowEdgeShape endArrow = arrowMxCell.getEndArrow();

    outputNode.setAttribute("id", String.valueOf(id));
    outputNode.setAttribute("id", String.valueOf(id));
    outputNode.setAttribute("parent", String.valueOf(parent));
    outputNode.setAttribute("value", String.valueOf(value));
    outputNode.setAttribute("style", String.valueOf(style));
    outputNode.setAttribute("edge", String.valueOf(edge));
    outputNode.setAttribute("startArrow", String.valueOf(startArrow));
    outputNode.setAttribute("endArrow", String.valueOf(endArrow));
    outputNode.setName("mxCell");
  }
}
