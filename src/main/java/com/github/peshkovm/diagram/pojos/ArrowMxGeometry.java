package com.github.peshkovm.diagram.pojos;

import com.github.peshkovm.diagram.converters.ArrowMxGeometryConverter;
import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

@Data
@Root
@Convert(ArrowMxGeometryConverter.class)
public class ArrowMxGeometry {
  @Attribute private final String as = "geometry";
  @Element private final SourceMxPoint sourceMxPoint;
  @Element private final TargetMxPoint targetMxPoint;
}
