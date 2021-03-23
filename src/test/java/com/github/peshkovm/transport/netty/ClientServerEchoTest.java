package com.github.peshkovm.transport.netty;

import com.github.peshkovm.common.BaseIntegrationTest;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportController;
import com.github.peshkovm.transport.TransportServer;
import com.github.peshkovm.transport.TransportService;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClientServerEchoTest extends BaseIntegrationTest {

  private CountDownLatch countDownLatch;
  private IntegerMessage msg;

  @BeforeEach
  void setUpServer() {
    final TransportService serverTransportService = transportServices.get(0);
    final TransportServer clientTransportServer = transportServers.get(1);
    final TransportController transportController =
        nodes.get(0).getBeanFactory().getBean(TransportController.class);

    transportController.registerMessageHandler(
        IntegerMessage.class,
        receivedMessage -> {
          logger.debug("Server received: {}", receivedMessage);
          serverTransportService.connectToNode(clientTransportServer.localNode());
          serverTransportService.send(clientTransportServer.localNode(), receivedMessage);
        });
  }

  @BeforeEach
  void setUpClient() {
    final TransportService clientTransportService = transportServices.get(1);
    final TransportServer serverTransportServer = transportServers.get(0);
    final TransportController transportController =
        nodes.get(1).getBeanFactory().getBean(TransportController.class);

    transportController.registerMessageHandler(
        IntegerMessage.class,
        receivedMessage -> {
          logger.debug("Client received: {}", receivedMessage);
          countDownLatch.countDown();
        });
    clientTransportService.connectToNode(serverTransportServer.localNode());
  }

  @Test
  @DisplayName("Client should send message to server and receive it back")
  void clientShouldSendMessageToServerAndReceiveItBack() throws InterruptedException {
    final TransportService clientTransportService = transportServices.get(1);
    final DiscoveryNode serverDiscoveryNode = transportServers.get(0).localNode();
    final int exchangeCount = 5000;

    for (int exchangeNum = 0; exchangeNum < exchangeCount; exchangeNum++) {
      countDownLatch = new CountDownLatch(1);
      msg = new IntegerMessage(exchangeNum);
      clientTransportService.send(serverDiscoveryNode, msg);
      countDownLatch.await();
    }
  }

  @Test
  @DisplayName("Client should send message to server and receive it back concurrently")
  void clientShouldSendMessageToServerAndReceiveItBackConcurrently() throws Exception {
    final TransportService clientTransportService = transportServices.get(1);
    final DiscoveryNode serverDiscoveryNode = transportServers.get(0).localNode();
    final int exchangeCount = 1000;

    countDownLatch = new CountDownLatch(exchangeCount);
    executeConcurrently(
        (threadNum, numOfCores) -> {
          for (int exchangeNum = threadNum;
              exchangeNum < exchangeCount;
              exchangeNum += numOfCores) {
            msg = new IntegerMessage(exchangeNum);
            clientTransportService.send(serverDiscoveryNode, msg);
          }
        });
    countDownLatch.await();
  }
}
