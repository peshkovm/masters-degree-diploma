package com.github.peshkovm.diagram.pojos;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Data
@Root(name = "mxfile")
public class MxFile {

  @Attribute private final boolean compressed = false;
  @Element private final Diagram diagram;
}
