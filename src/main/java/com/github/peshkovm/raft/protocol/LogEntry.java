package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import lombok.Data;

@Data
public class LogEntry implements Message {

  private final Message command;
  private final long session;

  public LogEntry(Message command, long session) {
    this.command = command;
    this.session = session;
  }
}
