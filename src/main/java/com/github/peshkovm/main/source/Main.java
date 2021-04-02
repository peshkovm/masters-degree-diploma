package com.github.peshkovm.main.source;

import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.commutative.GCounterCmRDT;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.node.ExternalClusterFactory;
import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

  protected static final Logger logger = LogManager.getLogger();
  private static CrdtService crdtService;

  public static void main(String[] args) {
    InternalNode internalNode = null;
    try {
      final String crdtId = "countOfLikes";
      internalNode = ExternalClusterFactory.getInternalNode();
      crdtService = internalNode.getBeanFactory().getBean(CrdtService.class);

      internalNode.start();

      final boolean isCreated = createResource(crdtId, ResourceType.GCounter);

      if (isCreated) {
        logger.info("Crdt was created on all nodes");
      } else {
        logger.info("Crdt was not created on one of the nodes");
        System.exit(1);
      }

      final GCounterCmRDT sourceGCounter =
          crdtService.crdtRegistry().crdt(crdtId, GCounterCmRDT.class);

    } catch (Exception e) {
      Objects.requireNonNull(internalNode);

      internalNode.stop();
      internalNode.close();

      e.printStackTrace();
    }
  }

  /**
   * Tries to create crdt of specified type and id on all nodes.
   *
   * <p>It's the blocking method. It will wait until crdt object is created on all nodes. If crdt
   * is failed to create on one of nodes, method returns immediately with false.
   *
   * @param crdt identity of crdt object
   * @param crdtType type of crdt object
   * @return true if success, false otherwise
   */
  private static boolean createResource(String crdt, ResourceType crdtType) {
    final boolean response = crdtService.addResource(crdt, crdtType).get();

    return response;
  }
}
