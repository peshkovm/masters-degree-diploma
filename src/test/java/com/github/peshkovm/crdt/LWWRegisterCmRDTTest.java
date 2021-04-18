package com.github.peshkovm.crdt;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.crdt.operationbased.LWWRegisterCmRDT;
import com.github.peshkovm.crdt.routing.ResourceType;
import io.vavr.collection.Vector;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LWWRegisterCmRDTTest extends BaseClusterTest {

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
    createResource("totalPrice", ResourceType.LWWRegisterCmRDT);
  }

  @Test
  @DisplayName("Should converge crdt on all replicas")
  @Disabled
  void shouldConvergeCrdtOnAllReplicas() throws Exception {
    final String crdtId = "totalPrice";
    final int timesToIncrement = 10_000;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(10);

    createResource(crdtId, ResourceType.LWWRegisterCmRDT);

    final Vector<LWWRegisterCmRDT> lwwRegisters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, LWWRegisterCmRDT.class));

    for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
      final LWWRegisterCmRDT sourceLWWRegister = lwwRegisters.get(0);
      sourceLWWRegister.assign(incrementNum + 1L);
    }

    //    executeConcurrently(
    //        (threadNum, numOfCores) -> {
    //          for (int incrementNum = threadNum;
    //              incrementNum < timesToIncrement;
    //              incrementNum += numOfCores) {
    //            final LWWRegisterCmRDT sourceLWWRegister = lwwRegisters.get(0);
    //            sourceLWWRegister.assign(incrementNum+1L);
    //          }
    //        });

    logger.info("Waiting for query");
    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!lwwRegisters.forAll(counter -> counter.value() == timesToIncrement)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      lwwRegisters.forEach(counter -> Assertions.assertEquals(counter.value(), timesToIncrement));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  @DisplayName("Should create multiple crdt with different id")
  void shouldCreateMultipleCrdtWithDifferentId() {
    createResource("totalPrice", ResourceType.LWWRegisterCmRDT);
    createResource("numOfViewers", ResourceType.LWWRegisterCmRDT);
  }

  @Test
  @DisplayName("Should converge multiple crdt on all replicas")
  void shouldConvergeMultipleCrdtOnAllReplicas() throws Exception {
    final String crdtId1 = "totalPrice";
    final String crdtId2 = "numOfViewers";
    final int timesToIncrement1 = 1_000;
    final int timesToIncrement2 = 500;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(2);

    createResource(crdtId1, ResourceType.LWWRegisterCmRDT);
    createResource(crdtId2, ResourceType.LWWRegisterCmRDT);

    final Vector<LWWRegisterCmRDT> lwwRegisters1 =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId1, LWWRegisterCmRDT.class));

    final Vector<LWWRegisterCmRDT> lwwRegisters2 =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId2, LWWRegisterCmRDT.class));

    for (int incrementNum = 0; incrementNum < timesToIncrement1; incrementNum++) {
      final LWWRegisterCmRDT sourceLWWRegister = lwwRegisters1.get(0);
      sourceLWWRegister.assign(incrementNum + 1L);
    }

    for (int incrementNum = 0; incrementNum < timesToIncrement2; incrementNum++) {
      final LWWRegisterCmRDT sourceLWWRegister = lwwRegisters2.get(0);
      sourceLWWRegister.assign(incrementNum + 1L);
    }

    //    executeConcurrently(
    //        (threadNum, numOfCores) -> {
    //          for (int incrementNum = threadNum;
    //              incrementNum < timesToIncrement1;
    //              incrementNum += numOfCores) {
    //            final LWWRegisterCmRDT sourceLWWRegister = lwwRegisters1.get(0);
    //            sourceLWWRegister.assign(incrementNum+1L);
    //          }
    //        });

    //    executeConcurrently(
    //        (threadNum, numOfCores) -> {
    //          for (int incrementNum = threadNum;
    //              incrementNum < timesToIncrement2;
    //              incrementNum += numOfCores) {
    //            final LWWRegisterCmRDT sourceLWWRegister = lwwRegisters2.get(0);
    //            sourceLWWRegister.assign(incrementNum+1L);
    //          }
    //        });

    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!lwwRegisters1.forAll(counter -> counter.value() == timesToIncrement1)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }
    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!lwwRegisters2.forAll(counter -> counter.value() == timesToIncrement2)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      lwwRegisters1.forEach(counter -> Assertions.assertEquals(counter.value(), timesToIncrement1));
      lwwRegisters2.forEach(counter -> Assertions.assertEquals(counter.value(), timesToIncrement2));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  @DisplayName("Should converge crdt updating by multiple clients")
  void shouldConvergeCrdtUpdatingByMultipleClients() throws Exception {
    final String crdtId = "totalPrice";
    final int timesToIncrement = 10_000;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(10);
    final int numOfNodes = crdtServices.size();

    createResource(crdtId, ResourceType.LWWRegisterCmRDT);

    final Vector<LWWRegisterCmRDT> lwwRegisters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, LWWRegisterCmRDT.class));

    for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
      final LWWRegisterCmRDT sourceLWWRegister = lwwRegisters.get(incrementNum % numOfNodes);
      sourceLWWRegister.assign(incrementNum + 1L);
    }

    //    executeConcurrently(
    //        (threadNum, numOfCores) -> {
    //          for (int incrementNum = threadNum;
    //              incrementNum < timesToIncrement;
    //              incrementNum += numOfCores) {
    //            final LWWRegisterCmRDT sourceLWWRegister = lwwRegisters.get(incrementNum %
    // numOfNodes);
    //            sourceLWWRegister.assign(incrementNum+1L);
    //          }
    //        });

    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!lwwRegisters.forAll(counter -> counter.value() == timesToIncrement)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      lwwRegisters.forEach(counter -> Assertions.assertEquals(counter.value(), timesToIncrement));
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
