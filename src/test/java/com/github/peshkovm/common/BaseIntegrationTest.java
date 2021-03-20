package com.github.peshkovm.common;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.transport.TransportServer;
import com.github.peshkovm.transport.TransportService;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseIntegrationTest extends BaseTest {

  protected final Logger logger = LogManager.getLogger(getClass());
  protected List<InternalNode> nodes = new ArrayList<>();
  protected List<TransportService> transportServices = new ArrayList<>();
  protected List<TransportServer> transportServers = new ArrayList<>();

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
    nodes.add(node);
    transportServers.add(node.getBeanFactory().getBean(TransportServer.class));
    transportServices.add(node.getBeanFactory().getBean(TransportService.class));

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
    nodes = Lists.newArrayList();
    InternalClusterFactory.reset();
  }
}
