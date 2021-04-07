package com.github.peshkovm.common.diagram;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

class RootMxCellConverter implements Converter<RootMxCell> {

  @Override
  public RootMxCell read(InputNode node) throws Exception {
    return new RootMxCell();
  }

  @Override
  public void write(OutputNode node, RootMxCell rootMxCell) throws Exception {
    final int id = RootMxCell.getId();

    node.setAttribute("id", String.valueOf(id));
    node.setName("mxCell");
  }
}
