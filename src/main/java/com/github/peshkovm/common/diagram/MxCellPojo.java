package com.github.peshkovm.common.diagram;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;

@Data
@Root(name = "mxCell", strict = false)
public class MxCellPojo {

  @Transient
  private final String type;
  @Attribute
  private final int id;
  @Attribute
  private final int parent;
  @Attribute
  private final String value;
  @Attribute
  private final String style;
  @Attribute
  private final int vertex;
  @Attribute
  private final int edge;
  @Element
  private final MxGeometryPojo mxGeometry;
}
