package com.github.peshkovm.main.operationbased.lwwregister;

import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.operationbased.AbstractCmRDT;
import com.github.peshkovm.crdt.operationbased.LWWRegisterCmRDT;
import com.github.peshkovm.crdt.operationbased.protocol.DownstreamUpdate;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.main.common.TestUtilsWithDiagram;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportService;
import com.github.peshkovm.transport.netty.NettyTransportService;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
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
        "Should not converge when using udp protocol",
        "src/main/resources/diagram/operationbased/lwwregister/shouldNotConvergeWhenUsingUdpProtocol.xml",
        600,
        true,
        true);
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
    final String crdtId = "totalPrice";
    final int timesToIncrement = 10;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(2);

    createResource(crdtId, ResourceType.LWWRegisterCmRDT);

    final Vector<LWWRegisterCmRDT> lwwRegisters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, LWWRegisterCmRDT.class));

    for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
      final LWWRegisterCmRDT sourceLWWRegister = lwwRegisters.head();
      sourceLWWRegister.assign(incrementNum + 1L);

      if (incrementNum == timesToIncrement / 2) {
        final Class<AbstractCmRDT<Long, Long>> type =
            (Class<AbstractCmRDT<Long, Long>>) ((AbstractCmRDT) sourceLWWRegister).getClass();

        final DownstreamUpdate<Long, Long> downstreamUpdate =
            new DownstreamUpdate<>(crdtId, Option.of(System.nanoTime()), type, incrementNum + 1L);

        final DiscoveryNode nodeToSend =
            nodes.get(1).getBeanFactory().getBean(ClusterDiscovery.class).getSelf();

        transportService.send(nodeToSend, downstreamUpdate);
      }
    }

    logger.info("Waiting for query");
    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!lwwRegisters.forAll(register -> register.query() == timesToIncrement)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    lwwRegisters.forEach(
        register -> {
          if (register.query() != timesToIncrement) {
            logger.error("FAILURE");
            throw new AssertionError(
                "\nExpected :" + timesToIncrement + "\nActual   :" + register.query());
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
