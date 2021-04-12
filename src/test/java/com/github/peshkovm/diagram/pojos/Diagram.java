package com.github.peshkovm.diagram.pojos;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Data
@Root(name = "diagram")
public class Diagram {

  @Attribute private final String name;
  @Element private final MxGraphModel mxGraphModel;
}
