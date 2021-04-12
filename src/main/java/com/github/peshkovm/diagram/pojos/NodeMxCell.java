package com.github.peshkovm.diagram.pojos;

import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.diagram.converters.NodeMxCellConverter;
import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;
import org.simpleframework.xml.convert.Convert;

@Data
@Root
@Convert(NodeMxCellConverter.class)
public class NodeMxCell {
  @Attribute private final int id;
  @Attribute private final int parent = 1;
  @Attribute private final String value;
  @Transient private final DrawIOColor color;
  @Attribute private final String style;
  @Attribute private final int vertex = 1;
  @Element private final NodeMxGeometry mxGeometry;

  public NodeMxCell(int id, String value, DrawIOColor color, NodeMxGeometry mxGeometry) {
    this.id = id + 2;
    this.value = value;
    this.color = color;
    this.mxGeometry = mxGeometry;
    this.style =
        "shape=umlLifeline;"
            + "perimeter=lifelinePerimeter;"
            + "fillColor="
            + color.fillColor
            + ";strokeColor="
            + color.strokeColor
            + ";strokeWidth=1";
  }
}
