package com.github.peshkovm.raft;

import com.github.peshkovm.raft.protocol.ClientMessageFailure;
import com.github.peshkovm.raft.protocol.ClientMessage;
import com.github.peshkovm.raft.protocol.ClientMessageSuccessful;
import com.github.peshkovm.raft.protocol.ClientCommand;
import com.github.peshkovm.transport.TransportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RaftHandler {

  @Autowired
  public RaftHandler(TransportController transportController, Raft raft) {
    transportController.registerMessageHandler(ClientCommand.class, raft::apply);
    transportController.registerMessageHandler(ClientMessage.class, raft::apply);
    transportController.registerMessageHandler(ClientMessageSuccessful.class, raft::apply);
    transportController.registerMessageHandler(ClientMessageFailure.class, raft::apply);
  }
}
