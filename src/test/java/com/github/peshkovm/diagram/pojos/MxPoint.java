package com.github.peshkovm.diagram.pojos;

import lombok.Data;
import lombok.Getter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Data
@Root(name = "mxPoint")
public class MxPoint {

  @Attribute private final int x;
  @Attribute private final long y;
  @Attribute private final PointRole as;

  @Getter
  public enum PointRole {
    SOURCE("sourcePoint"),
    TARGET("sourcePoint");

    private final String name;

    PointRole(String name) {
      this.name = name;
    }
  }
}
