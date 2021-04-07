package com.github.peshkovm.crdt;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.common.diagram.DiagramBuilderSingleton;
import com.github.peshkovm.common.diagram.DrawIOColor;
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

    connectAllNodes();

    crdtServices = nodes.map(node -> node.getBeanFactory().getBean(CrdtService.class));
  }

  @Test
  @DisplayName("Should replicate crdt to all replicas")
  void shouldReplicateCrdtToAllReplicas() throws Exception {
    createResource("countOfLikes", ResourceType.GCounter);
  }

  @Test
  @DisplayName("Should converge crdt on all replicas")
  void shouldConvergeCrdtOnAllReplicas() throws Exception {
    final String crdtId = "countOfLikes";
    final int timesToIncrement = 10_000;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(10);

    createResource(crdtId, ResourceType.GCounter);

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

  @Test
  @DisplayName("Should create multiple crdt with different id")
  void shouldCreateMultipleCrdtWithDifferentId() {
    createResource("countOfLikes", ResourceType.GCounter);
    createResource("countOfViews", ResourceType.GCounter);
  }

  @Test
  @DisplayName("Should converge multiple crdt on all replicas")
  void shouldConvergeMultipleCrdtOnAllReplicas() throws Exception {
    final String crdtId1 = "countOfLikes";
    final String crdtId2 = "countOfViews";
    final int timesToIncrement1 = 1_000;
    final int timesToIncrement2 = 500;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(2);

    createResource(crdtId1, ResourceType.GCounter);
    createResource(crdtId2, ResourceType.GCounter);

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
      if (!gCounters1.forAll(counter -> counter.query() == timesToIncrement1)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }
    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!gCounters2.forAll(counter -> counter.query() == timesToIncrement2)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      gCounters1.forEach(counter -> Assertions.assertEquals(counter.query(), timesToIncrement1));
      gCounters2.forEach(counter -> Assertions.assertEquals(counter.query(), timesToIncrement2));
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

    createResource(crdtId, ResourceType.GCounter);

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

  @Test
  @DisplayName("Should get payloads from all nodes")
  void shouldGetPayloadsFromAllNodes() throws Exception {
    final String crdtId = "countOfLikes";
    final int timesToIncrement = 10_000;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(10);

    createResource(crdtId, ResourceType.GCounter);

    final Vector<GCounterCmRDT> gCounters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, GCounterCmRDT.class));

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
      if (!crdtServices
          .head()
          .queryAllNodes(crdtId, GCounterCmRDT.class)
          .get()
          .forAll(payload -> payload == timesToIncrement)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      crdtServices
          .head()
          .queryAllNodes(crdtId, GCounterCmRDT.class)
          .get()
          .forEach(payload -> Assertions.assertEquals(payload, timesToIncrement));
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
  private void createResource(String crdt, ResourceType crdtType) {
    crdtServices.head().addResource(crdt, crdtType).get();
  }
}
