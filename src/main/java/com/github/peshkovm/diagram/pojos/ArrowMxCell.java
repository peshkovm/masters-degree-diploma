package com.github.peshkovm.diagram.pojos;

import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.diagram.converters.ArrowMxCellConverter;
import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;
import org.simpleframework.xml.convert.Convert;

@Data
@Root
@Convert(ArrowMxCellConverter.class)
public class ArrowMxCell {
  @Attribute private final int id;
  @Attribute private final int parent = 1;
  @Attribute private final String value;
  @Attribute private final String style;
  @Attribute private final int edge = 1;
  @Attribute private final ArrowEdgeShape startArrow;
  @Attribute private final ArrowEdgeShape endArrow;
  @Transient private final DrawIOColor color;

  @Element(name = "arrowMxGeometry")
  private final ArrowMxGeometry mxGeometry;

  public ArrowMxCell(
      int id,
      String value,
      ArrowEdgeShape startArrow,
      ArrowEdgeShape endArrow,
      DrawIOColor color,
      ArrowMxGeometry arrowMxGeometry) {
    this.id = id + 2;
    this.value = value;
    this.startArrow = startArrow;
    this.endArrow = endArrow;
    this.color = color;
    this.mxGeometry = arrowMxGeometry;
    this.style =
        "strokeWidth=1;"
            + "startArrow="
            + this.startArrow
            + ";endArrow="
            + endArrow
            + ";startFill=1;"
            + "fillColor="
            + color.strokeColor
            + ";strokeColor="
            + color.strokeColor
            + ";";
  }

  public enum ArrowEdgeShape {
    OVAL,
    CLASSIC,
    CROSS
  }
}
