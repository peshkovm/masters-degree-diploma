package com.github.peshkovm.diagram;

import com.typesafe.config.Config;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
public class DiagramNodeMeta {
  private final String nodeName;

  @Autowired
  public DiagramNodeMeta(Config config) {
    this.nodeName = config.getString("diagram.node.name");
  }
}
