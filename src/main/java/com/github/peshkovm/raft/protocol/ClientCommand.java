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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ClientCommand)) {
      return false;
    }

    ClientCommand that = (ClientCommand) o;

    return getSession() == that.getSession();
  }

  @Override
  public int hashCode() {
    return (int) (getSession() ^ (getSession() >>> 32));
  }
}
