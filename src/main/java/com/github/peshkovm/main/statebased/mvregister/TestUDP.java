package com.github.peshkovm.main.statebased.mvregister;

import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.statebased.AbstractCvRDT;
import com.github.peshkovm.crdt.statebased.MVRegisterCvRDT;
import com.github.peshkovm.crdt.statebased.MVRegisterCvRDT.Pair;
import com.github.peshkovm.crdt.statebased.protocol.Payload;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.MessageType;
import com.github.peshkovm.main.common.TestUtilsWithDiagram;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportService;
import com.github.peshkovm.transport.netty.NettyTransportService;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;
import java.util.concurrent.TimeUnit;

public class TestUDP extends TestUtilsWithDiagram {

  private Vector<CrdtService> crdtServices;
  private TransportService transportService;

  void setUpNodes() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    checkNumberOfCreatedNodes();
    addNodesToDiagram();
    connectAllNodes();
    crdtServices = nodes.map(node -> node.getBeanFactory().getBean(CrdtService.class));
    transportService = nodes.head().getBeanFactory().getBean(TransportService.class);
  }

  @Override
  protected DiagramFactorySingleton getDiagramInstance() {
    return DiagramFactorySingleton.getInstance(
        "Should converge when connection will be established",
        "src/main/resources/diagram/statebased/mvregister/shouldNotConvergeWhenUsingUdpProtocol.xml",
        600,
        160,
        true,
        true,
        MessageType.ADD_RESOURCE,
        MessageType.COMMAND_RESULT);
  }

  public static void main(String[] args) throws Exception {
    final TestUDP testInstance = new TestUDP();
    try {
      testInstance.setUpNodes();
      testInstance.shouldNotConvergeWhenUsingUdpProtocol();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      testInstance.tearDownNodes();
    }
  }

  void shouldNotConvergeWhenUsingUdpProtocol() throws Exception {
    final String crdtId = "text";
    final int timesToIncrement = 5;
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

      final String sourcePayloadStr =
          sourceMVRegister
              .value()
              .map(Pair::getValue)
              .reduceOption((str1, str2) -> str1 + str2)
              .get();

      sourceMVRegister.assign(sourcePayloadStr + alphabet.get(incrementNum));
      sourceMVRegister.replicatePayload();

      if (incrementNum == timesToIncrement / 2) {
        final Class<AbstractCvRDT<String, Set<Pair>, Set<Pair>>> type =
            (Class<AbstractCvRDT<String, Set<Pair>, Set<Pair>>>)
                ((AbstractCvRDT) sourceMVRegister).getClass();
        final Set<Pair> sourcePayload = sourceMVRegister.value();
        final Payload<String, Set<Pair>, Set<Pair>> payloadMsg =
            new Payload<>(sourcePayload, crdtId, type);

        final DiscoveryNode nodeToSend =
            nodes.get(1).getBeanFactory().getBean(ClusterDiscovery.class).getSelf();

        transportService.send(nodeToSend, payloadMsg);
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
