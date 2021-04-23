package com.github.peshkovm.diagram.pojos;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Data
@Root
public class NodeMxGeometry {

  @Attribute private final long x;
  @Attribute private final long y;
  @Attribute private final long width;
  @Attribute private final long height;
  @Attribute private final String as = "geometry";
}
