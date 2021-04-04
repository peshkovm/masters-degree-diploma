package com.github.peshkovm.main.source;

import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.commutative.GCounterCmRDT;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.node.ExternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

  protected static final Logger logger = LogManager.getLogger();
  private static CrdtService crdtService;

  public static void main(String[] args) {
    InternalNode internalNode = null;
    try {
      final String crdtId = "countOfLikes";
      final long timesToIncrement = 100;
      final long numOfSecondsToWait = TimeUnit.SECONDS.toMillis(10);

      internalNode = ExternalClusterFactory.getInternalNode("192.168.0.106", 8801);
      crdtService = internalNode.getBeanFactory().getBean(CrdtService.class);

      internalNode.start();

      createResource(crdtId, ResourceType.GCounter);

      final GCounterCmRDT sourceGCounter =
          crdtService.crdtRegistry().crdt(crdtId, GCounterCmRDT.class);

      for (int incrementNum = 0; incrementNum < timesToIncrement; incrementNum++) {
        sourceGCounter.increment();
      }

      logger.info("Waiting for query");
      for (int i = 0; i < numOfSecondsToWait / 100; i++) {
        if (!crdtService
            .queryAllNodes(crdtId, GCounterCmRDT.class)
            .get()
            .forAll(payload -> payload == timesToIncrement)) {
          TimeUnit.MILLISECONDS.sleep(100);
        } else {
          break;
        }
      }

      if (crdtService
          .queryAllNodes(crdtId, GCounterCmRDT.class)
          .get()
          .forAll(payload -> payload == timesToIncrement)) {
        logger.info("Crdts correctly replicated on all nodes");
      } else {
        throw new IllegalStateException("Crdts did not replicated on all nodes");
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (internalNode != null) {
        internalNode.stop();
        internalNode.close();
      }
    }
  }

  /**
   * Tries to create crdt of specified type and id on all nodes.
   *
   * <p>It's the blocking method. It will wait until crdt object is created on all nodes. If crdt
   * is
   * failed to create on one of nodes, method returns immediately with false.
   *
   * @param crdt identity of crdt object
   * @param crdtType type of crdt object
   */
  private static void createResource(String crdt, ResourceType crdtType) {
    crdtService.addResource(crdt, crdtType).get();
  }
}
