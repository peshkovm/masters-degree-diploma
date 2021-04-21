package com.github.peshkovm.diagram.pojos;

import com.github.peshkovm.diagram.converters.NodeMxGeometryConverter;
import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

@Data
@Root
@Convert(NodeMxGeometryConverter.class)
public class NodeMxGeometry {

  @Attribute private final long x;
  @Attribute private final long y;
  @Attribute private final int width;
  @Attribute private final int height;
  @Attribute private final String as = "geometry";
}
