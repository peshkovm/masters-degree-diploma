package com.github.peshkovm.common.diagram;

import java.util.List;
import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Data
@Root(name = "mxGeometry")
public class MxGeometryPojo {

  @Attribute
  private final int x;
  @Attribute
  private final int y;
  @Attribute
  private final int width;
  @Attribute
  private final int height;
  @Attribute
  private final String as;

  @ElementList(inline = true)
  private final List<MxPoint> mxPoints;
}
