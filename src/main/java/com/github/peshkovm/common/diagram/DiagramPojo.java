package com.github.peshkovm.common.diagram;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Data
@AllArgsConstructor
@Root(name = "diagram")
public class DiagramPojo {

  @Attribute
  private String name;
  @Element
  private final MxGraphModelPojo mxGraphModel;
}
