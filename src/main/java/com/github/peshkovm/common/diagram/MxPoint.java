package com.github.peshkovm.common.diagram;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Data
@AllArgsConstructor
@Root(name = "mxPoint")
public class MxPoint {

  @Attribute
  private final int x;
  @Attribute
  private long y;
  @Attribute
  private final String as;
}
