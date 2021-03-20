package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import lombok.Data;

@Data
public class ClientMessage implements Message {

  private final Message command;
  private final long session;

  public ClientMessage(Message command, long session) {
    this.command = command;
    this.session = session;
  }
}
