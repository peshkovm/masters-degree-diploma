package com.github.peshkovm.common;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.transport.TransportServer;
import com.github.peshkovm.transport.TransportService;
import io.vavr.collection.List;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseIntegrationTest extends BaseTest {

  protected final Logger logger = LogManager.getLogger(getClass());
  protected List<InternalNode> nodes = List.empty();
  protected List<TransportService> transportServices = List.empty();
  protected List<TransportServer> transportServers = List.empty();

  @BeforeEach
  void setUpNodes() {
    createAndStartNode(); // Server
    createAndStartNode(); // Client
  }

  /**
   * Creates and starts node on same JVM with specified port.
   */
  protected void createAndStartNode() {
    final InternalNode node = InternalClusterFactory.createInternalNode();
    nodes = nodes.append(node);
    transportServers =
        transportServers.append(node.getBeanFactory().getBean(TransportServer.class));
    transportServices =
        transportServices.append(node.getBeanFactory().getBean(TransportService.class));

    node.start();
  }

  @Data
  public static class IntegerMessage implements Message {

    private final Integer value;

    public IntegerMessage(Integer value) {
      this.value = value;
    }
  }

  @AfterEach
  protected void tearDownNodes() {
    nodes.forEach(LifecycleComponent::stop);
    nodes.forEach(LifecycleComponent::close);
    nodes = List.empty();
    transportServices = List.empty();
    transportServers = List.empty();
    InternalClusterFactory.reset();
  }
}
