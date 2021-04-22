package com.github.peshkovm.diagram.pojos;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Data
@Root
public class ArrowMxGeometry {
  @Attribute private final String as = "geometry";

  @Element(name = "sourceMxPoint")
  private final SourceMxPoint sourceMxPoint;

  @Element(name = "targetMxPoint")
  private final TargetMxPoint targetMxPoint;
}
