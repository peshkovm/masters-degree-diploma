package com.github.peshkovm.main.operationbased.gcounter;

import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.operationbased.GCounterCmRDT;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.MessageType;
import com.github.peshkovm.main.common.TestUtilsWithDiagram;
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
        "exampleOfWork",
        "src/main/resources/diagram/operationbased/gcounter/exampleOfWork.xml",
        600,
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
    final String crdtId = "countOfLikes";
    final int timesToIncrement = 3;
    final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(2);

    createResource(crdtId, ResourceType.GCounterCmRDT);

    final Vector<GCounterCmRDT> gCounters =
        crdtServices
            .map(CrdtService::crdtRegistry)
            .map(crdtRegistry -> crdtRegistry.crdt(crdtId, GCounterCmRDT.class));

    for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
      final GCounterCmRDT sourceGCounter = gCounters.get(incrementNum % gCounters.size());
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

  private void createResource(String crdt, ResourceType crdtType) {
    crdtServices.head().addResource(crdt, crdtType).get();
  }
}
