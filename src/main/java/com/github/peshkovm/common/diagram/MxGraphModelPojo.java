package com.github.peshkovm.common.diagram;

import lombok.Data;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Data
@Root(name = "mxGraphModel")
public class MxGraphModelPojo {

  @Element
  private final RootPojo root;
}
