package com.github.peshkovm.main.statebased.mvregister;

import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.statebased.MVRegisterCvRDT;
import com.github.peshkovm.crdt.statebased.MVRegisterCvRDT.Pair;
import com.github.peshkovm.main.common.TestUtils;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.netty.NettyTransportService;
import io.vavr.collection.Vector;
import java.util.concurrent.TimeUnit;

public class TestNodeDisconnection extends TestUtils {

  private Vector<CrdtService> crdtServices;

  public static void main(String[] args) throws Exception {
    final TestNodeDisconnection testInstance = new TestNodeDisconnection();
    try {
      testInstance.setUpNodes();
      testInstance.shouldConvergeWhenConnectionWillBeEstablished();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      testInstance.tearDownNodes();
    }
  }

  void shouldConvergeWhenConnectionWillBeEstablished() throws Exception {
    final String crdtId = "text";
    final int timesToIncrement = 100;
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

      if (incrementNum == 25) {
        partition(nodes.get(1));
      }
      if (incrementNum == 50) {
        recoverFromPartition(nodes.get(1));
      }
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

    mvRegisters.forEach(
        register -> {
          if (!register
              .value()
              .map(Pair::getValue)
              .reduce((str1, str2) -> str1 + str2)
              .equals(alphabetStr)) {

            logger.error("FAILURE");
            throw new AssertionError(
                "\nExpected :"
                    + alphabetStr
                    + "\nActual   :"
                    + register.value().map(Pair::getValue).reduce((str1, str2) -> str1 + str2));
          }
        });
    logger.info("SUCCESSFUL");
  }

  void setUpNodes() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    connectAllNodes();

    crdtServices = nodes.map(node -> node.getBeanFactory().getBean(CrdtService.class));
  }

  private void createResource(String crdt, ResourceType crdtType) {
    crdtServices.head().addResource(crdt, crdtType).get();
  }

  private void partition(InternalNode nodeToDisconnectFrom) {
    final InternalNode sourceNode = nodes.head();

    final NettyTransportService sourceTransportService =
        sourceNode.getBeanFactory().getBean(NettyTransportService.class);

    sourceTransportService.disconnectFromNode(
        nodeToDisconnectFrom.getBeanFactory().getBean(ClusterDiscovery.class).getSelf());
  }

  private void recoverFromPartition(InternalNode nodeToConnectTo) {
    final InternalNode sourceNode = nodes.head();

    final NettyTransportService sourceTransportService =
        sourceNode.getBeanFactory().getBean(NettyTransportService.class);

    sourceTransportService.connectToNode(
        nodeToConnectTo.getBeanFactory().getBean(ClusterDiscovery.class).getSelf());
  }
}
