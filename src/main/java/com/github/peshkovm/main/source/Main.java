package com.github.peshkovm.main.source;

import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.commutative.GCounterCmRDT;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.node.ExternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

  protected static final Logger logger = LogManager.getLogger();
  private static CrdtService crdtService;

  public static void main(String[] args) {
    InternalNode internalNode = null;
    try {
      final String crdtId = "countOfLikes";
      internalNode = ExternalClusterFactory.getInternalNode("192.168.0.106", 8801);
      crdtService = internalNode.getBeanFactory().getBean(CrdtService.class);

      internalNode.start();

      createResource(crdtId, ResourceType.GCounter);

      final GCounterCmRDT sourceGCounter =
          crdtService.crdtRegistry().crdt(crdtId, GCounterCmRDT.class);

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
   * @return true if success, false otherwise
   */
  private static void createResource(String crdt, ResourceType crdtType) {
    crdtService.addResource(crdt, crdtType).get();
  }
}
