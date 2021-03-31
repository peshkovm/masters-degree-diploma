package com.github.peshkovm.crdt;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.crdt.commutative.GCounterCmRDT;
import com.github.peshkovm.crdt.routing.ResourceType;
import io.vavr.collection.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClusterCrdtTest extends BaseClusterTest {

  private Vector<CrdtService> crdtServices;

  @BeforeEach
  void setUpNodes() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    crdtServices = nodes.map(node -> node.getBeanFactory().getBean(CrdtService.class));
  }

  @Test
  @DisplayName("Should replicate crdt to all replicas")
  void shouldReplicateCrdtToAllReplicas() throws Exception {
    final boolean isCreated = createResource("countOfLikes", ResourceType.GCounter);

    Assertions.assertTrue(isCreated);
  }

  @Test
  @DisplayName("Should converge crdt on all replicas")
  void shouldConvergeCrdtOnAllReplicas() throws Exception {
    final String crdtId = "countOfLikes";
    final int timeToIncrement = 5;
    final boolean isCreated = createResource(crdtId, ResourceType.GCounter);

    if (!isCreated) {
      logger.error("Crdt was not created");
      return;
    }

    final Vector<GCounterCmRDT> gCounters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, GCounterCmRDT.class));

    for (int incrementNum = 0; incrementNum < timeToIncrement; incrementNum++) {
      final GCounterCmRDT sourceGCounter = gCounters.get(0);
      sourceGCounter.increment();
    }
  }

  private boolean createResource(String crdt, ResourceType crdtType) throws Exception {
    final boolean response = crdtServices.head().addResource(crdt, crdtType).get();

    return response;
  }
}
