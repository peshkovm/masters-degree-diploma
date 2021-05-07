package com.github.peshkovm.crdt;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.statebased.AbstractCvRDT;
import com.github.peshkovm.crdt.statebased.MVRegisterCvRDT;
import com.github.peshkovm.crdt.statebased.MVRegisterCvRDT.Pair;
import io.vavr.collection.Vector;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MVRegisterCvRDTTest extends BaseClusterTest {
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
    createResource("text", ResourceType.MVRegisterCvRDT);
  }

  @Test
  @DisplayName("Should converge crdt on all replicas")
  @Disabled
  void shouldConvergeCrdtOnAllReplicas() throws Exception {
    final String crdtId = "text";
    final int timesToIncrement = 1000;
    final Vector<String> alphabet = Vector.range(0, timesToIncrement).map(i -> i + " ").toVector();
    final String alphabetStr = alphabet.reduce((str1, str2) -> str1 + str2);
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(10);

    createResource(crdtId, ResourceType.MVRegisterCvRDT);

    final Vector<MVRegisterCvRDT> mvRegisters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, MVRegisterCvRDT.class));

    for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
      final MVRegisterCvRDT sourceMVRegister = mvRegisters.get(0);

      final String sourcePayload =
          sourceMVRegister
              .value()
              .map(Pair::getValue)
              .reduceOption((str1, str2) -> str1 + str2)
              .get();

      sourceMVRegister.assign(sourcePayload + alphabet.get(incrementNum));
      sourceMVRegister.replicatePayload();
    }

    logger.info("Waiting for query");
    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!mvRegisters.forAll(
          register ->
              register
                  .value()
                  .map(Pair::getValue)
                  .reduce((str1, str2) -> str1 + str2)
                  .equals(alphabetStr))) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      mvRegisters.forEach(
          register ->
              Assertions.assertEquals(
                  register.value().map(Pair::getValue).reduce((str1, str2) -> str1 + str2),
                  alphabetStr));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  @DisplayName("Should create multiple crdt with different id")
  void shouldCreateMultipleCrdtWithDifferentId() {
    createResource("text1", ResourceType.MVRegisterCvRDT);
    createResource("text2", ResourceType.MVRegisterCvRDT);
  }

  @Test
  @DisplayName("Should converge multiple crdt on all replicas")
  void shouldConvergeMultipleCrdtOnAllReplicas() throws Exception {
    final String crdtId1 = "text1";
    final String crdtId2 = "text2";
    final int timesToIncrement1 = 1_000;
    final Vector<String> alphabet1 =
        Vector.range(0, timesToIncrement1).map(i -> i + " ").toVector();
    final String alphabetStr1 = alphabet1.reduce((str1, str2) -> str1 + str2);
    final int timesToIncrement2 = 500;
    final Vector<String> alphabet2 =
        Vector.range(0, timesToIncrement2).map(i -> i + " ").toVector();
    final String alphabetStr2 = alphabet2.reduce((str1, str2) -> str1 + str2);
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(10);

    createResource(crdtId1, ResourceType.MVRegisterCvRDT);
    createResource(crdtId2, ResourceType.MVRegisterCvRDT);

    final Vector<MVRegisterCvRDT> mvRegisters1 =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId1, MVRegisterCvRDT.class));

    final Vector<MVRegisterCvRDT> mvRegisters2 =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId2, MVRegisterCvRDT.class));

    for (int incrementNum = 0; incrementNum < timesToIncrement1; incrementNum++) {
      final MVRegisterCvRDT sourceMVRegister = mvRegisters1.get(0);

      final String sourcePayload =
          sourceMVRegister
              .value()
              .map(Pair::getValue)
              .reduceOption((str1, str2) -> str1 + str2)
              .get();

      sourceMVRegister.assign(sourcePayload + alphabet1.get(incrementNum));
      sourceMVRegister.replicatePayload();
    }

    for (int incrementNum = 0; incrementNum < timesToIncrement2; incrementNum++) {
      final MVRegisterCvRDT sourceMVRegister = mvRegisters2.get(0);

      final String sourcePayload =
          sourceMVRegister
              .value()
              .map(Pair::getValue)
              .reduceOption((str1, str2) -> str1 + str2)
              .get();

      sourceMVRegister.assign(sourcePayload + alphabet2.get(incrementNum));
      sourceMVRegister.replicatePayload();
    }

    logger.info("Waiting for query");
    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!mvRegisters1.forAll(
          register ->
              register
                  .value()
                  .map(Pair::getValue)
                  .reduce((str1, str2) -> str1 + str2)
                  .equals(alphabetStr1))) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!mvRegisters2.forAll(
          register ->
              register
                  .value()
                  .map(Pair::getValue)
                  .reduce((str1, str2) -> str1 + str2)
                  .equals(alphabetStr2))) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      mvRegisters1.forEach(
          register ->
              Assertions.assertEquals(
                  register.value().map(Pair::getValue).reduce((str1, str2) -> str1 + str2),
                  alphabetStr1));
      mvRegisters2.forEach(
          register ->
              Assertions.assertEquals(
                  register.value().map(Pair::getValue).reduce((str1, str2) -> str1 + str2),
                  alphabetStr2));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  @DisplayName("Should converge crdt updating by multiple clients")
  void shouldConvergeCrdtUpdatingByMultipleClients() throws Exception {
    final String crdtId = "text";
    final int timesToIncrement = 3;
    final Vector<String> alphabet = Vector.range(0, timesToIncrement).map(i -> i + " ").toVector();
    final String alphabetStr = alphabet.reduce((str1, str2) -> str1 + str2);
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(10);
    final int numOfNodes = crdtServices.size();

    createResource(crdtId, ResourceType.MVRegisterCvRDT);

    final Vector<MVRegisterCvRDT> mvRegisters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, MVRegisterCvRDT.class));

    for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
      final MVRegisterCvRDT sourceMVRegister = mvRegisters.get(incrementNum % numOfNodes);

      sourceMVRegister.assign(alphabet.get(incrementNum));
    }

    mvRegisters.forEach(AbstractCvRDT::replicatePayload);

    //    executeConcurrently(
    //        (threadNum, numOfCores) -> {
    //          for (int incrementNum = threadNum;
    //              incrementNum < timesToIncrement;
    //              incrementNum += numOfCores) {
    //            final MVRegisterCvRDT sourceGCounter = mvRegisters.get(0);
    //            sourceGCounter.assign(incrementNum + "");
    //            sourceGCounter.replicatePayload();
    //          }
    //        });

    logger.info("Waiting for query");
    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!mvRegisters.forAll(
          register ->
              register
                  .value()
                  .toSortedSet(
                      (pair1, pair2) -> {
                        final String vvStr1 =
                            pair1
                                .getVersionVector()
                                .getVersions()
                                .map(String::valueOf)
                                .reduce((str1, str2) -> str1 + str2);

                        final String vvStr2 =
                            pair2
                                .getVersionVector()
                                .getVersions()
                                .map(String::valueOf)
                                .reduce((str1, str2) -> str1 + str2);

                        return Integer.parseInt(vvStr1, 2) - Integer.parseInt(vvStr2, 2);
                      })
                  .map(Pair::getValue)
                  .reduce((str1, str2) -> str1 + str2)
                  .equals(alphabetStr))) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    try {
      mvRegisters.forEach(
          register ->
              Assertions.assertEquals(
                  register
                      .value()
                      .toSortedSet(
                          (pair1, pair2) -> {
                            final String vvStr1 =
                                pair1
                                    .getVersionVector()
                                    .getVersions()
                                    .map(String::valueOf)
                                    .reduce((str1, str2) -> str1 + str2);

                            final String vvStr2 =
                                pair2
                                    .getVersionVector()
                                    .getVersions()
                                    .map(String::valueOf)
                                    .reduce((str1, str2) -> str1 + str2);

                            return Integer.parseInt(vvStr1, 2) - Integer.parseInt(vvStr2, 2);
                          })
                      .map(Pair::getValue)
                      .reduce((str1, str2) -> str1 + str2),
                  alphabetStr));
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
   */
  private void createResource(String crdt, ResourceType crdtType) {
    crdtServices.head().addResource(crdt, crdtType).get();
  }
}
