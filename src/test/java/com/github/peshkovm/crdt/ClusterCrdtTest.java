package com.github.peshkovm.crdt;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.routing.fsm.AddResourceResponse;
import io.vavr.collection.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClusterCrdtTest extends BaseClusterTest {

  private Vector<CrdtService> crdtServices;

  @BeforeEach
  void setUpNodes() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    crdtServices = nodes.map(node -> node.getBeanFactory().getBean(CrdtService.class));
  }

  @Test
  @DisplayName("Should replicate crdt to all replicas")
  void shouldReplicateCrdtToAllReplicas() throws Exception {
    final AddResourceResponse response = createResource("countOfLikes", ResourceType.GCounter);

    Assertions.assertEquals(response.getResourceId(), "countOfLikes");
    Assertions.assertEquals(response.getResourceType(), ResourceType.GCounter);
  }

  private AddResourceResponse createResource(String crdt, ResourceType crdtType) throws Exception {
    final AddResourceResponse response = crdtServices.head().addResource(crdt, crdtType).get();

    return response;
  }
}
