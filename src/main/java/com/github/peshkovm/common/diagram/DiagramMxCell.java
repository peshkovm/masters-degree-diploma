package com.github.peshkovm.common.diagram;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

@Data
@Root
@Convert(DiagramMxCellConverter.class)
public class DiagramMxCell {

  @Attribute
  private static final int id = 1;
  @Attribute
  private static final int parent = 0;

  public static int getId() {
    return id;
  }

  public static int getParent() {
    return parent;
  }
}
