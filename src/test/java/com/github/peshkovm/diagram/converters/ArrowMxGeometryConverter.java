package com.github.peshkovm.diagram.converters;

import com.github.peshkovm.diagram.pojos.ArrowMxGeometry;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class ArrowMxGeometryConverter implements Converter<ArrowMxGeometry> {

  @Override
  public ArrowMxGeometry read(InputNode node) throws Exception {
    throw new UnsupportedOperationException("Deserialization is not supported");
  }

  @Override
  public void write(OutputNode outputNode, ArrowMxGeometry arrowMxGeometry) throws Exception {
    final String as = arrowMxGeometry.getAs();
    outputNode.setAttribute("as", as);
    outputNode.setName("mxGeometry");
  }
}
