package com.github.peshkovm.common.diagram;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Data
@Root(name = "mxfile")
public class MxFilePojo {

  @Attribute
  private final boolean compressed;
  @Element
  private final DiagramPojo diagram;
}
