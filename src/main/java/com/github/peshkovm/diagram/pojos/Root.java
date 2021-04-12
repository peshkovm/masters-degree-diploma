package com.github.peshkovm.diagram.pojos;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

@Data
@org.simpleframework.xml.Root(name = "root")
public class Root {

  @Element private final RootMxCell rootMxCell;
  @Element private final DiagramMxCell diagramMxCell;

  @ElementList(inline = true)
  private final List<NodeMxCell> nodes;

  @ElementList(inline = true)
  private final List<ArrowMxCell> arrows;

  public Root(RootMxCell rootMxCell, DiagramMxCell diagramMxCell) {
    this.rootMxCell = rootMxCell;
    this.diagramMxCell = diagramMxCell;
    this.nodes = new ArrayList<>();
    arrows = new ArrayList<>();
  }
}
