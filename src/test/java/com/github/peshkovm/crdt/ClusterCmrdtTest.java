package com.github.peshkovm.crdt;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.crdt.operationbased.GCounterCmRDT;
import com.github.peshkovm.crdt.routing.ResourceType;
import io.vavr.collection.Vector;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClusterCmrdtTest extends BaseClusterTest {

  private Vector<CrdtService> crdtServices;

  @BeforeEach
  void setUpNodes() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    connectAllNodes();

    crdtServices = nodes.map(node -> node.getBeanFactory().getBean(CrdtService.class));
  }

  @Test
  @DisplayName("Should replicate crdt to all replicas")
  @Disabled
  void shouldReplicateCrdtToAllReplicas() {
    createResource("countOfLikes", ResourceType.GCounterCmRDT);
  }

  @Test
  @DisplayName("Should converge crdt on all replicas")
  @Disabled
  void shouldConvergeCrdtOnAllReplicas() throws Exception {
    final String crdtId = "countOfLikes";
    final int timesToIncrement = 10_000;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(10);

    createResource(crdtId, ResourceType.GCounterCmRDT);

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

    logger.info("Waiting for query");
    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!gCounters.forAll(counter -> counter.value() == timesToIncrement)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      gCounters.forEach(counter -> Assertions.assertEquals(counter.value(), timesToIncrement));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  @DisplayName("Should create multiple crdt with different id")
  void shouldCreateMultipleCrdtWithDifferentId() {
    createResource("countOfLikes", ResourceType.GCounterCmRDT);
    createResource("countOfViews", ResourceType.GCounterCmRDT);
  }

  @Test
  @DisplayName("Should converge multiple crdt on all replicas")
  void shouldConvergeMultipleCrdtOnAllReplicas() throws Exception {
    final String crdtId1 = "countOfLikes";
    final String crdtId2 = "countOfViews";
    final int timesToIncrement1 = 1_000;
    final int timesToIncrement2 = 500;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(2);

    createResource(crdtId1, ResourceType.GCounterCmRDT);
    createResource(crdtId2, ResourceType.GCounterCmRDT);

    final Vector<GCounterCmRDT> gCounters1 =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId1, GCounterCmRDT.class));

    final Vector<GCounterCmRDT> gCounters2 =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId2, GCounterCmRDT.class));

    // for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
    //   final GCounterCmRDT sourceGCounter = gCounters.get(0);
    //   sourceGCounter.increment();
    // }

    executeConcurrently(
        (threadNum, numOfCores) -> {
          for (int incrementNum = threadNum;
              incrementNum < timesToIncrement1;
              incrementNum += numOfCores) {
            final GCounterCmRDT sourceGCounter = gCounters1.get(0);
            sourceGCounter.increment();
          }
        });

    executeConcurrently(
        (threadNum, numOfCores) -> {
          for (int incrementNum = threadNum;
              incrementNum < timesToIncrement2;
              incrementNum += numOfCores) {
            final GCounterCmRDT sourceGCounter = gCounters2.get(0);
            sourceGCounter.increment();
          }
        });

    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!gCounters1.forAll(counter -> counter.value() == timesToIncrement1)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }
    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!gCounters2.forAll(counter -> counter.value() == timesToIncrement2)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      gCounters1.forEach(counter -> Assertions.assertEquals(counter.value(), timesToIncrement1));
      gCounters2.forEach(counter -> Assertions.assertEquals(counter.value(), timesToIncrement2));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  @DisplayName("Should converge crdt updating by multiple clients")
  void shouldConvergeCrdtUpdatingByMultipleClients() throws Exception {
    final String crdtId = "countOfLikes";
    final int timesToIncrement = 10_000;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(10);
    final int numOfNodes = crdtServices.size();

    createResource(crdtId, ResourceType.GCounterCmRDT);

    final Vector<GCounterCmRDT> gCounters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, GCounterCmRDT.class));

    //    for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
    //      final GCounterCmRDT sourceGCounter = gCounters.get(incrementNum % numOfNodes);
    //      sourceGCounter.increment();
    //    }

    executeConcurrently(
        (threadNum, numOfCores) -> {
          for (int incrementNum = threadNum;
              incrementNum < timesToIncrement;
              incrementNum += numOfCores) {
            final GCounterCmRDT gCounter = gCounters.get(incrementNum % numOfNodes);
            gCounter.increment();
          }
        });

    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!gCounters.forAll(counter -> counter.value() == timesToIncrement)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      gCounters.forEach(counter -> Assertions.assertEquals(counter.value(), timesToIncrement));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Tries to create crdt of specified type and id on all nodes.
   *
   * <p>It's the blocking method. It will wait until crdt object is created on all nodes. If crdt is
   * failed to create on one of nodes, method returns immediately with false.
   *
   * @param crdt identity of crdt object
   * @param crdtType type of crdt object
   * @return true if success, false otherwise
   */
  private void createResource(String crdt, ResourceType crdtType) {
    crdtServices.head().addResource(crdt, crdtType).get();
  }
}
