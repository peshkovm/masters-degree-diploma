package com.github.peshkovm.crdt;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.crdt.routing.ResourceType;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClusterCrdtTest extends BaseClusterTest {

  private List<CrdtService> crdtServices;

  @BeforeEach
  void setUpNodes() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    crdtServices =
        nodes.stream()
            .map(node -> node.getBeanFactory().getBean(CrdtService.class))
            .collect(Collectors.toList());
  }

  @Disabled
  @Test
  @DisplayName("Should replicate crdt to all replicas")
  void shouldReplicateCrdtToAllReplicas() throws InterruptedException {
    createResource(0, ResourceType.GCounter);
  }

  private void createResource(int crdt, ResourceType crdtType) throws InterruptedException {
    final CrdtService leaderCrdtService = crdtServices.get(0);

    leaderCrdtService.addResource(crdt, crdtType).sync();
  }
}
