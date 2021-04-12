package com.github.peshkovm.diagram.pojos;

import lombok.Data;
import org.simpleframework.xml.Element;

@Data
@org.simpleframework.xml.Root(name = "mxGraphModel")
public class MxGraphModel {

  @Element private final Root root;
}
