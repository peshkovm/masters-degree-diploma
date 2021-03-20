package com.github.peshkovm.raft;

import com.github.peshkovm.common.Match;
import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.raft.protocol.AppendEntry;
import com.github.peshkovm.raft.protocol.AppendSuccessful;
import com.github.peshkovm.raft.protocol.ClientMessage;
import com.github.peshkovm.raft.protocol.LogEntry;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportController;
import com.github.peshkovm.transport.TransportService;
import io.vavr.concurrent.Future;
import io.vavr.concurrent.Promise;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class Raft extends AbstractLifecycleComponent {

  private final TransportService transportService;
  private final TransportController transportController;
  private final ReentrantLock lock;
  private final ClusterDiscovery clusterDiscovery;
  private final List<LogEntry> replicatedLog;
  private final Map<Long, Promise<Message>> sessionCommands;
  private SourceState sourceState;
  private ReplicaState replicaState;

  private final Match.Mapper<Message, State> mapper =
      Match.<Message, State>map()
          .when(ClientMessage.class, sourceState::handle)
          .when(AppendEntry.class, replicaState::handle)
          .when(AppendSuccessful.class, sourceState::handle)
          .build();

  @Autowired
  public Raft(
      TransportService transportService,
      ClusterDiscovery clusterDiscovery,
      TransportController transportController) {
    this.transportService = transportService;
    this.clusterDiscovery = clusterDiscovery;
    this.transportController = transportController;
    this.lock = new ReentrantLock();
    this.replicatedLog = new ArrayList<>();
    this.sessionCommands = new ConcurrentHashMap<>();

    final RaftMetadata meta = new RaftMetadata(clusterDiscovery);
    this.sourceState = new SourceState(meta);
    this.replicaState = new ReplicaState(meta);
  }

  public Future<Message> command(Message command) {
    final Promise<Message> promise = Promise.make();
    Promise<Message> prev;
    long session;
    do {
      session = ThreadLocalRandom.current().nextLong();
      prev = sessionCommands.putIfAbsent(session, promise);
    } while (prev != null);

    long finalSession = session;
    logger.info("Client command with session: {}", () -> finalSession);

    final ClientMessage clientMessage = new ClientMessage(command, session);
    apply(clientMessage);

    return promise.future();
  }

  public void apply(Message event) {
    lock.lock();
    try {
      mapper.apply(event);
    } finally {
      lock.unlock();
    }
  }

  private void send(DiscoveryNode node, Message message) {
    if (node.equals(clusterDiscovery.getSelf())) {
      transportController.dispatch(message);
    } else {
      transportService.send(node, message);
    }
  }

  private abstract static class State {

    @Getter
    private final RaftMetadata meta;

    private State(RaftMetadata meta) {
      this.meta = meta;
    }
  }

  private class SourceState extends State {

    public SourceState(RaftMetadata meta) {
      super(meta);
    }

    public State handle(ClientMessage message) {
      LogEntry entry = new LogEntry(message.getCommand(), message.getSession());
      replicatedLog.add(entry);

      logger.debug("Source node appended command: [{}] to replicated log", message::getCommand);

      sendEntriesToAllReplicas();
      return this;
    }

    public State handle(AppendSuccessful message) {
      logger.info("Leader received {}", message);

      final LogEntry logEntry =
          replicatedLog.stream()
              .filter(entry -> entry.getSession() == message.getSession())
              .findFirst()
              .get();

      return maybeCommitEntry(logEntry);
    }

    private void sendEntriesToAllReplicas() {
      logger.debug("Source node send entries to replicas: {}", () -> getMeta().getDiscoveryNodes());

      getMeta().getDiscoveryNodesWithout(clusterDiscovery.getSelf()).forEach(this::sendEntries);
    }

    private void sendEntries(DiscoveryNode follower) {
      replicatedLog.forEach(
          entry -> {
            AppendEntry append = new AppendEntry(clusterDiscovery.getSelf(), entry);
            send(follower, append);
          });
    }

    private State maybeCommitEntry(LogEntry logEntry) {
      final Promise<Message> promise = sessionCommands.remove(logEntry.getSession());
      if (promise != null) {
        promise.success(null);
      }

      return this;
    }
  }

  private class ReplicaState extends State {

    private ReplicaState(RaftMetadata meta) {
      super(meta);
    }

    public State handle(AppendEntry message) {
      return appendEntries(message);
    }

    private State appendEntries(AppendEntry message) {
      logger.debug("Follower appended entry: [{}] to replicated log", message::getEntry);
      replicatedLog.add(message.getEntry());

      logger.debug(() -> "Follower send AppendSuccessful");
      final AppendSuccessful response =
          new AppendSuccessful(clusterDiscovery.getSelf(), message.getEntry().getSession());
      send(message.getDiscoveryNode(), response);

      return this;
    }
  }

  @Override
  protected void doStart() {
  }

  @Override
  protected void doStop() {
  }

  @Override
  protected void doClose() {
  }
}
