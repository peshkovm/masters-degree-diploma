package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import lombok.Data;

@Data
public class ClientCommand implements Message {

  private final Message command;
  private final long session;

  public ClientCommand(Message command, long session) {
    this.command = command;
    this.session = session;
  }
}
