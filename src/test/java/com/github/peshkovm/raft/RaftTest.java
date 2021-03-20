package com.github.peshkovm.raft;

import com.github.peshkovm.common.BaseClusterTest;
import com.github.peshkovm.common.codec.Message;
import io.vavr.concurrent.Future;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class RaftTest extends BaseClusterTest {

  private List<Raft> raftList;
  private List<RegisterClient> clients;

  @BeforeEach
  void setUpNodes() {
    createAndStartInternalNode();
    createAndStartInternalNode();
    createAndStartInternalNode();

    raftList =
        nodes.stream()
            .map(n -> n.getBeanFactory().getBean(Raft.class))
            .collect(Collectors.toList());
    clients =
        nodes.stream()
            .map(n -> n.getBeanFactory().getBean(RegisterClient.class))
            .collect(Collectors.toList());
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
  public static class RegisterClient {

    private final Raft raft;

    @Autowired
    public RegisterClient(Raft raft) {
      this.raft = raft;
    }

    public Future<String> setValue(String value) {
      return raft.command(new RegisterValue(value))
          .filter(m -> m instanceof RegisterValue)
          .map(m -> ((RegisterValue) m).value);
    }

    public Future<String> getValue() {
      return raft.command(new RegisterValue(""))
          .filter(msg -> msg instanceof RegisterValue)
          .map(msg -> ((RegisterValue) msg).getValue());
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
