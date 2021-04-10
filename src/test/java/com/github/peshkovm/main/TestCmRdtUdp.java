package com.github.peshkovm.main;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.common.diagram.DiagramBuilderSingleton;
import com.github.peshkovm.common.diagram.DrawIOColor;
import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.commutative.AbstractCmRDT;
import com.github.peshkovm.crdt.commutative.GCounterCmRDT;
import com.github.peshkovm.crdt.commutative.protocol.DownstreamUpdate;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportService;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestCmRdtUdp extends BaseClusterTest {

  private Vector<CrdtService> crdtServices;
  private DiagramBuilderSingleton diagramBuilder;
  private TransportService transportService;

  @BeforeEach
  void setUpNodes() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    connectAllNodes();

    crdtServices = nodes.map(node -> node.getBeanFactory().getBean(CrdtService.class));
    diagramBuilder = nodes.head().getBeanFactory().getBean(DiagramBuilderSingleton.class);
    transportService = nodes.head().getBeanFactory().getBean(TransportService.class);
    diagramBuilder.setActive(true);

    for (int i = 0; i < nodes.size(); i++) {
      diagramBuilder.addNode("Node" + (i + 1), DrawIOColor.values()[i]);
    }
  }

  @Test
  @DisplayName("Shouldn not converge when using udp protocol")
  void shouldNotConvergeWhenUsingUdpProtocol() throws Exception {
    diagramBuilder.setDiagramName("Shouldn't converge when using udp protocol");
    diagramBuilder.setOutputFileName("shouldNotConvergeWhenUsingUdpProtocol.xml");

    final String crdtId = "countOfLikes";
    final int timesToIncrement = 3;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(2);

    createResource(crdtId, ResourceType.GCounter);

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
            new DownstreamUpdate<>(crdtId, Option.none(), type, 1L, System.nanoTime());

        final DiscoveryNode nodeToSend =
            nodes.get(1).getBeanFactory().getBean(ClusterDiscovery.class).getSelf();

        transportService.send(nodeToSend, downstreamUpdate);
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

    diagramBuilder.build();

    try {
      gCounters.forEach(counter -> Assertions.assertEquals(counter.query(), timesToIncrement));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void createResource(String crdt, ResourceType crdtType) {
    crdtServices.head().addResource(crdt, crdtType).get();
  }
}
