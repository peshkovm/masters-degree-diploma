package com.github.peshkovm.raft;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.raft.protocol.CommandResult;
import com.github.peshkovm.raft.resource.ResourceFSM;
import com.github.peshkovm.raft.resource.ResourceRegistry;
import io.vavr.collection.Vector;
import io.vavr.concurrent.Future;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class RaftTest extends BaseClusterTest {

  private Vector<RegisterClient> clients;

  @BeforeEach
  void setUpNodes() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    connectAllNodes();

    clients = nodes.map(n -> n.getBeanFactory().getBean(RegisterClient.class));
  }

  @Test
  @DisplayName("Should send ClientMessage to follower and receive it back")
  void shouldSendClientMessageToFollowerAndReceiveItBack() {
    final String value1 = clients.get(0).getValue().get();
    Assertions.assertEquals("", value1);

    final String value2 = clients.get(1).setValue("Hello World").get();
    Assertions.assertEquals("Hello World", value2);
  }

  @Component
  public static class RegisterResource implements ResourceFSM {

    private final Logger logger = LogManager.getLogger();
    private final String value = "";

    @Autowired
    public RegisterResource(ResourceRegistry registry) {
      registry.registerHandler(RegisterValue.class, this::handle);
    }

    public CommandResult handle(RegisterValue registerValue) {
      logger.info("Applying RegisterValue");
      final RegisterValue result = new RegisterValue(registerValue.getValue());

      return new CommandResult(result, true);
    }
  }

  @Component
  public static class RegisterClient {

    private final Raft raft;

    @Autowired
    public RegisterClient(Raft raft) {
      this.raft = raft;
    }

    public Future<String> setValue(String value) {
      return raft.command(new RegisterValue(value))
          .map(Vector::ofAll)
          .map(commandResults -> commandResults.map(CommandResult::getResult))
          .filter(
              commandResults -> commandResults.forAll(result -> result instanceof RegisterValue))
          .map(commandResults -> commandResults.map(result -> (RegisterValue) result))
          .map(registerValues -> registerValues.map(response -> (String) response.getValue()))
          .map(Vector::head);
    }

    public Future<String> getValue() {
      return raft.command(new RegisterValue(""))
          .map(Vector::ofAll)
          .map(commandResults -> commandResults.map(CommandResult::getResult))
          .filter(
              commandResults -> commandResults.forAll(result -> result instanceof RegisterValue))
          .map(commandResults -> commandResults.map(result -> (RegisterValue) result))
          .map(registerValues -> registerValues.map(response -> (String) response.getValue()))
          .map(Vector::head);
    }
  }

  @Data
  public static class RegisterValue implements Message {

    private final String value;

    public RegisterValue(String value) {
      this.value = value;
    }
  }
}
