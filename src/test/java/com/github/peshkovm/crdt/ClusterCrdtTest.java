package com.github.peshkovm.crdt;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.crdt.commutative.GCounterCmRDT;
import com.github.peshkovm.crdt.routing.ResourceType;
import io.vavr.collection.Vector;
import java.util.concurrent.TimeUnit;
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
  void shouldReplicateCrdtToAllReplicas() {
    final boolean isCreated = createResource("countOfLikes", ResourceType.GCounter);

    Assertions.assertTrue(isCreated);
  }

  @Test
  @DisplayName("Should converge crdt on all replicas")
  void shouldConvergeCrdtOnAllReplicas() throws Exception {
    final String crdtId = "countOfLikes";
    final int timesToIncrement = 100_000;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(2);

    final boolean isCreated = createResource(crdtId, ResourceType.GCounter);

    if (!isCreated) {
      logger.error("Crdt was not created");
      return;
    }

    final Vector<GCounterCmRDT> gCounters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, GCounterCmRDT.class));

    // for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
    //   final GCounterCmRDT sourceGCounter = gCounters.get(0);
    //   sourceGCounter.increment();
    // }

    executeConcurrently(
        (threadNum, numOfCores) -> {
          for (int incrementNum = threadNum;
              incrementNum < timesToIncrement;
              incrementNum += numOfCores) {
            final GCounterCmRDT sourceGCounter = gCounters.get(0);
            sourceGCounter.increment();
          }
        });

    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!gCounters.forAll(counter -> counter.query() == timesToIncrement)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      gCounters.forEach(counter -> Assertions.assertEquals(counter.query(), timesToIncrement));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Tries to create crdt of specified type and id on all nodes.
   *
   * <p>It's the blocking method. It will wait until crdt object is created on all nodes. If crdt
   * is
   * failed to create on one of nodes, method returns immediately with false.
   *
   * @param crdt identity of crdt object
   * @param crdtType type of crdt object
   * @return true if success, false otherwise
   */
  private boolean createResource(String crdt, ResourceType crdtType) {
    final boolean response = crdtServices.head().addResource(crdt, crdtType).get();

    return response;
  }
}
