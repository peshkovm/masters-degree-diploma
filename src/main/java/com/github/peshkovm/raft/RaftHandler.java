package com.github.peshkovm.raft;

import com.github.peshkovm.raft.protocol.AppendMessage;
import com.github.peshkovm.raft.protocol.AppendSuccessful;
import com.github.peshkovm.raft.protocol.ClientMessage;
import com.github.peshkovm.transport.TransportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RaftHandler {

  @Autowired
  public RaftHandler(TransportController transportController, Raft raft) {
    transportController.registerMessageHandler(ClientMessage.class, raft::apply);
    transportController.registerMessageHandler(AppendMessage.class, raft::apply);
    transportController.registerMessageHandler(AppendSuccessful.class, raft::apply);
  }
}
