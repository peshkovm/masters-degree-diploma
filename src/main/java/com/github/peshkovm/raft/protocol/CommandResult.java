package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import lombok.Data;

@Data
public class CommandResult implements Message {

  protected final Message result;
  protected final boolean isSuccessful;

  public CommandResult(Message result, boolean isSuccessful) {
    this.isSuccessful = isSuccessful;
    this.result = result;
  }
}
