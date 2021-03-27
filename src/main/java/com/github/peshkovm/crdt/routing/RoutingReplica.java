package com.github.peshkovm.crdt.routing;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class RoutingReplica implements Message {

  private final long id;
  private final DiscoveryNode member;

  public RoutingReplica(long id, DiscoveryNode member) {
    this(id, member, State.OPENED);
  }

  public RoutingReplica(long id, DiscoveryNode member, State state) {
    this.id = id;
    this.member = member;
    this.state = state;
  }

  private final State state;

  public enum State {
    OPENED,
    CLOSED
  }
}
