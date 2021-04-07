package com.github.peshkovm.common.diagram;

import java.util.List;
import lombok.Data;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Data
@Root(name = "root")
public class RootPojo {

  @Element
  private final RootMxCell rootMxCell;
  @Element
  private final DiagramMxCell diagramMxCell;

  @ElementList(inline = true)
  private final List<MxCellPojo> mxCells;

  public RootPojo(
      RootMxCell rootMxCell, DiagramMxCell diagramMxCell, List<MxCellPojo> mxCells) {
    this.rootMxCell = rootMxCell;
    this.diagramMxCell = diagramMxCell;
    this.mxCells = mxCells;
  }
}
