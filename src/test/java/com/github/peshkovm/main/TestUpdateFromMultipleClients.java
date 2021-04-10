package com.github.peshkovm.main;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.common.diagram.DiagramBuilderSingleton;
import com.github.peshkovm.common.diagram.DrawIOColor;
import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.commutative.GCounterCmRDT;
import com.github.peshkovm.crdt.routing.ResourceType;
import io.vavr.collection.Vector;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestUpdateFromMultipleClients extends BaseClusterTest {

  private Vector<CrdtService> crdtServices;
  private DiagramBuilderSingleton diagramBuilder;

  @BeforeEach
  void setUpNodes() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    connectAllNodes();

    crdtServices = nodes.map(node -> node.getBeanFactory().getBean(CrdtService.class));
    diagramBuilder = nodes.head().getBeanFactory().getBean(DiagramBuilderSingleton.class);
    diagramBuilder.setActive(true);

    for (int i = 0; i < nodes.size(); i++) {
      diagramBuilder.addNode("Node" + (i + 1), DrawIOColor.values()[i]);
    }
  }

  @Test
  @DisplayName("Should converge updates from multiple clients")
  void shouldConvergeUpdatesFromMultipleClients() throws Exception {
    diagramBuilder.setDiagramName("Should converge updates from multiple clients");
    diagramBuilder.setOutputFileName("shouldConvergeUpdatesFromMultipleClients.xml");

    final String crdtId = "countOfLikes";
    final int timesToIncrement = 3;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(2);

    createResource(crdtId, ResourceType.GCounter);

    final Vector<GCounterCmRDT> gCounters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, GCounterCmRDT.class));

    for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
      final GCounterCmRDT sourceGCounter = gCounters.get(incrementNum % nodes.size());
      sourceGCounter.increment();
    }

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

    diagramBuilder.build();
  }

  private void createResource(String crdt, ResourceType crdtType) {
    crdtServices.head().addResource(crdt, crdtType).get();
  }
}
