package com.github.peshkovm.main.operationbased.gcounter;

import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.operationbased.GCounterCmRDT;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.MessageType;
import com.github.peshkovm.main.common.TestUtilsWithDiagram;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.netty.NettyTransportService;
import io.vavr.collection.Vector;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

public class TestNodeDisconnection extends TestUtilsWithDiagram {

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

  //  @Override
  //  public DiagramFactorySingleton getInstance() {
  //    return DiagramFactorySingleton.getInstance(
  //        "Should converge when connection will be established",
  //        "src/main/resources/diagram/shouldConvergeWhenConnectionWillBeEstablished.xml",
  //        600,
  //        true,
  //        true,
  //        MessageType.ADD_RESOURCE,
  //        MessageType.COMMAND_RESULT);
  //  }

  @Configuration
  @Profile("diagram")
  public static class DiagramConfiguration {

    @Bean
    public DiagramFactorySingleton diagramFactorySingleton() {
      return DiagramFactorySingleton.getInstance(
          "Should converge when connection will be established",
          "src/main/resources/diagram/shouldConvergeWhenConnectionWillBeEstablished.xml",
          600,
          true,
          true,
          MessageType.ADD_RESOURCE,
          MessageType.COMMAND_RESULT);
    }
  }

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

      if (incrementNum == timesToIncrement / 4) {
        partition(nodes.get(1));
      }
      if (incrementNum == timesToIncrement / 2) {
        recoverFromPartition(nodes.get(1));
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

  private void createResource(String crdt, ResourceType crdtType) {
    crdtServices.head().addResource(crdt, crdtType).get();
  }
}
