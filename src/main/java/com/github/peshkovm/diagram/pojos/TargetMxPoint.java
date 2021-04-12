package com.github.peshkovm.diagram.pojos;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Data
@Root(name = "mxPoint")
public class TargetMxPoint {
  @Attribute private final long x;
  @Attribute private final long y;
  @Attribute private final String as = "targetPoint";
}
