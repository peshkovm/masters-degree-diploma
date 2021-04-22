package com.github.peshkovm.diagram.converters;

import com.github.peshkovm.diagram.pojos.RootMxCell;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class RootMxCellConverter implements Converter<RootMxCell> {

  @Override
  public RootMxCell read(InputNode node) throws Exception {
    throw new UnsupportedOperationException("Deserialization is not supported");
  }

  @Override
  public void write(OutputNode outputNode, RootMxCell rootMxCell) throws Exception {
    final int id = RootMxCell.getId();

    outputNode.setAttribute("id", String.valueOf(id));
    outputNode.setName("mxCell");
  }
}
