package com.github.peshkovm.diagram;

import lombok.Data;

@Data
public class DiagramNodeMeta {
  private final String nodeName;

  public DiagramNodeMeta(String nodeName) {
    this.nodeName = nodeName;
  }
}
