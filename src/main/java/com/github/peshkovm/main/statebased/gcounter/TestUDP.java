package com.github.peshkovm.main.statebased.gcounter;

import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.statebased.AbstractCvRDT;
import com.github.peshkovm.crdt.statebased.GCounterCvRDT;
import com.github.peshkovm.crdt.statebased.protocol.Payload;
import com.github.peshkovm.main.common.TestUtils;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportService;
import com.github.peshkovm.transport.netty.NettyTransportService;
import io.vavr.collection.Vector;
import java.util.concurrent.TimeUnit;

public class TestUDP extends TestUtils {

  private Vector<CrdtService> crdtServices;
  private TransportService transportService;

  public static void main(String[] args) throws Exception {
    final TestUDP testInstance = new TestUDP();
    try {
      testInstance.setUpNodes();
      testInstance.shouldNotConvergeWhenUsingUdpProtocol();
    } finally {
      testInstance.tearDownNodes();
    }
  }

  void shouldNotConvergeWhenUsingUdpProtocol() throws Exception {
    final String crdtId = "countOfLikes";
    final int timesToIncrement = 100;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(2);

    createResource(crdtId, ResourceType.GCounterCvRDT);

    final Vector<GCounterCvRDT> gCounters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, GCounterCvRDT.class));

    for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
      final GCounterCvRDT sourceGCounter = gCounters.head();
      sourceGCounter.increment();
      sourceGCounter.replicatePayload();

      if (incrementNum == timesToIncrement / 2) {
        final Class<AbstractCvRDT<Long, Long, Vector<Long>>> type =
            (Class<AbstractCvRDT<Long, Long, Vector<Long>>>)
                ((AbstractCvRDT) sourceGCounter).getClass();
        final Vector<Long> sourcePayload = sourceGCounter.merge(Vector.fill(nodes.size(), 0L));
        final Payload<Long, Long, Vector<Long>> payloadMsg =
            new Payload<>(sourcePayload, crdtId, type);

        final DiscoveryNode nodeToSend =
            nodes.get(1).getBeanFactory().getBean(ClusterDiscovery.class).getSelf();

        transportService.send(nodeToSend, payloadMsg);
      }
    }

    logger.info("Waiting for query");
    for (int i = 0; i < numOfSecondsToWait / 100; i++) {
      if (!gCounters.forAll(counter -> counter.query() == timesToIncrement)) {
        TimeUnit.MILLISECONDS.sleep(100);
      } else {
        break;
      }
    }

    gCounters.forEach(
        counter -> {
          if (counter.query() != timesToIncrement) {
            logger.error("FAILURE");
            throw new AssertionError(
                "\nExpected :" + timesToIncrement + "\nActual   :" + counter.query());
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
    transportService = nodes.head().getBeanFactory().getBean(TransportService.class);
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
