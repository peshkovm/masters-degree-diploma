package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class ClientMessageFailure implements Message {

  private final DiscoveryNode discoveryNode;
  private final ClientCommand clientCommand;
  private final CommandResult commandResult;

  public ClientMessageFailure(
      DiscoveryNode discoveryNode, ClientCommand clientCommand, CommandResult commandResult) {
    this.discoveryNode = discoveryNode;
    this.clientCommand = clientCommand;
    this.commandResult = commandResult;
  }
}
