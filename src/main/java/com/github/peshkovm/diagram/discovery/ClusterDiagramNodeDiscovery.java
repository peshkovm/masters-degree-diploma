package com.github.peshkovm.diagram.discovery;

import com.github.peshkovm.diagram.DiagramNodeMeta;
import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.DiscoveryNode;
import com.typesafe.config.Config;
import io.vavr.collection.Map;
import io.vavr.collection.TreeMap;
import java.util.List;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("diagram")
public class ClusterDiagramNodeDiscovery {

  private static final Logger logger = LogManager.getLogger();
  @Getter private final DiagramNodeMeta self;
  @Getter private final Map<DiscoveryNode, DiagramNodeMeta> replicas;

  @Autowired
  public ClusterDiagramNodeDiscovery(Config config, ClusterDiscovery clusterDiscovery) {
    Map<DiscoveryNode, DiagramNodeMeta> replicas = TreeMap.empty();
    DiagramNodeMeta self =
        new DiagramNodeMeta(
            config.getString("diagram.node.name"),
            DrawIOColor.valueOf(config.getString("diagram.node.color")));

    final List<String> nodesNames = config.getStringList("diagram.nodes");
    final List<String> nodesColors = config.getStringList("diagram.colors");

    for (int i = 0; i < clusterDiscovery.getDiscoveryNodes().size(); i++) {
      final DiscoveryNode discoveryNode = clusterDiscovery.getDiscoveryNodes().toList().get(i);

      if (!self.getNodeName().equals(nodesNames.get(i))
          && !self.getNodeColor().equals(DrawIOColor.valueOf(nodesColors.get(i)))) {
        replicas =
            replicas.put(
                discoveryNode,
                new DiagramNodeMeta(nodesNames.get(i), DrawIOColor.valueOf(nodesColors.get(i))));
      }
    }

    this.self = self;
    this.replicas = replicas;

    logger.info("Self = {}", () -> this.self);
    logger.info("Replicas = {}", () -> this.replicas);
  }
}
