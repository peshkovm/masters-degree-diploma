package com.github.peshkovm.main.operationbased.gcounter;

import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.operationbased.AbstractCmRDT;
import com.github.peshkovm.crdt.operationbased.GCounterCmRDT;
import com.github.peshkovm.crdt.operationbased.protocol.DownstreamUpdate;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.MessageType;
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
        "src/main/resources/diagram/operationbased/gcounter/shouldNotConvergeWhenUsingUdpProtocol.xml",
        600,
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
    final String crdtId = "countOfLikes";
    final int timesToIncrement = 10;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(2);

    createResource(crdtId, ResourceType.GCounterCmRDT);

    final Vector<GCounterCmRDT> gCounters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, GCounterCmRDT.class));

    for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
      final GCounterCmRDT sourceGCounter = gCounters.head();
      sourceGCounter.increment();

      if (incrementNum == timesToIncrement / 2) {
        final Class<AbstractCmRDT<Long, Long>> type =
            (Class<AbstractCmRDT<Long, Long>>) ((AbstractCmRDT) sourceGCounter).getClass();

        final DownstreamUpdate<Long, Long> downstreamUpdate =
            new DownstreamUpdate<>(crdtId, Option.none(), type, 1L);

        final DiscoveryNode nodeToSend =
            nodes.get(1).getBeanFactory().getBean(ClusterDiscovery.class).getSelf();

        transportService.send(nodeToSend, downstreamUpdate);
      }
    }

    logger.info("Waiting for query");
    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!gCounters
          .zipWithIndex()
          .forAll(
              tuple2 -> {
                final GCounterCmRDT counter = tuple2._1();
                final Integer index = tuple2._2();

                return index == 1
                    ? counter.query() == timesToIncrement + 1
                    : counter.query() == timesToIncrement;
              })) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    gCounters
        .zipWithIndex()
        .forEach(
            tuple2 -> {
              final GCounterCmRDT counter = tuple2._1();
              final Integer index = tuple2._2();

              if (!(index == 1
                  ? counter.query() == timesToIncrement + 1
                  : counter.query() == timesToIncrement)) {
                logger.error("FAILURE");
                throw new AssertionError(
                    "\nExpected :" + timesToIncrement + "\nActual   :" + counter.query());
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
