package com.github.peshkovm.main.statebased.mvregister;

import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.statebased.AbstractCvRDT;
import com.github.peshkovm.crdt.statebased.MVRegisterCvRDT;
import com.github.peshkovm.crdt.statebased.MVRegisterCvRDT.Pair;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.MessageType;
import com.github.peshkovm.main.common.TestUtilsWithDiagram;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.netty.NettyTransportService;
import io.vavr.collection.Vector;
import java.util.concurrent.TimeUnit;

public class ExampleOfWork extends TestUtilsWithDiagram {

  private Vector<CrdtService> crdtServices;

  void setUpNodes() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    checkNumberOfCreatedNodes();
    addNodesToDiagram();
    connectAllNodes();

    crdtServices = nodes.map(node -> node.getBeanFactory().getBean(CrdtService.class));
  }

  @Override
  protected DiagramFactorySingleton getDiagramInstance() {
    return DiagramFactorySingleton.getInstance(
        "Should converge when connection will be established",
        "src/main/resources/diagram/statebased/mvregister/exampleOfWork.xml",
        600,
        320,
        true,
        true,
        MessageType.ADD_RESOURCE,
        MessageType.COMMAND_RESULT);
  }

  public static void main(String[] args) throws Exception {
    final ExampleOfWork testInstance = new ExampleOfWork();
    try {
      testInstance.setUpNodes();
      testInstance.exampleOfWork();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      testInstance.tearDownNodes();
    }
  }

  void exampleOfWork() throws Exception {
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
    mvRegisters.forEach(AbstractCvRDT::replicatePayload);

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

    mvRegisters.forEach(
        register -> {
          if (!register
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
              .equals(alphabetStr)) {
            logger.error("FAILURE");
            throw new AssertionError(
                "\nExpected :"
                    + alphabetStr
                    + "\nActual   :"
                    + register
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
                        .reduce((str1, str2) -> str1 + str2));
          }
        });

    logger.info("SUCCESSFUL");
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
