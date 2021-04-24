package com.github.peshkovm.diagram;

import com.github.peshkovm.diagram.commons.DrawIOColor;
import lombok.Data;

@Data
public class DiagramNodeMeta {
  private final String nodeName;
  private final DrawIOColor nodeColor;

  public DiagramNodeMeta(String nodeName, DrawIOColor nodeColor) {
    this.nodeName = nodeName;
    this.nodeColor = nodeColor;
  }
}
