package com.github.peshkovm.raft;

import static com.github.peshkovm.raft.RaftState.FOLLOWER;
import static com.github.peshkovm.raft.RaftState.LEADER;

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
import com.typesafe.config.Config;
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
  private volatile State state;
  private final ClusterDiscovery clusterDiscovery;
  private final List<LogEntry> replicatedLog;
  private final Config config;
  private final Map<Long, Promise<Message>> sessionCommands;

  private final Match.Mapper<Message, State> mapper =
      Match.<Message, State>map()
          .when(ClientMessage.class, e -> state.handle(e))
          .when(AppendEntry.class, e -> state.handle(e))
          .build();

  @Autowired
  public Raft(
      TransportService transportService,
      ClusterDiscovery clusterDiscovery,
      TransportController transportController,
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") Config config) {
    this.transportService = transportService;
    this.clusterDiscovery = clusterDiscovery;
    this.transportController = transportController;
    this.config = config;
    this.lock = new ReentrantLock();
    this.replicatedLog = new ArrayList<>();

    initState();
    sessionCommands = new ConcurrentHashMap<>();
  }

  private void initState() {
    final RaftMetadata meta = new RaftMetadata(clusterDiscovery);

    if (config.getBoolean("raft.is_leader")) {
      this.state = new SourceState(meta);
    } else {
      // this.state = new FollowerState(meta);
    }
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

  private RaftState currentState() {
    return state.getState();
  }

  private abstract class State {

    @Getter
    private final RaftMetadata meta;

    private State(RaftMetadata meta) {
      this.meta = meta;
      state = this;
    }

    public abstract RaftState getState();

    public abstract State handle(ClientMessage message);

    public abstract State handle(AppendEntry message);

    public State handle(AppendSuccessful message) {
      logger.debug("Unhandled {} in {}", () -> message, this::getState);
      return this;
    }
  }

  private class SourceState extends State {

    public SourceState(RaftMetadata meta) {
      super(meta);
    }

    @Override
    public RaftState getState() {
      return LEADER;
    }

    private void sendEntriesToFollowerNodes() {
      logger.debug(
          "Leader send entries to follower nodes: {}", () -> getMeta().getDiscoveryNodes());

      getMeta().getDiscoveryNodesWithout(clusterDiscovery.getSelf()).forEach(this::sendEntries);
    }

    private void sendEntries(DiscoveryNode follower) {
      final List<LogEntry> entries = new ArrayList<>(replicatedLog);
      // replicatedLog.clear();

      entries.forEach(
          entry -> {
            AppendEntry append = new AppendEntry(clusterDiscovery.getSelf(), entry);
            send(follower, append);
          });
    }

    @Override
    public State handle(ClientMessage message) {
      LogEntry entry = new LogEntry(message.getCommand(), message.getSession());
      replicatedLog.add(entry);

      logger.debug("Leader appended command: [{}] to replicated log", message::getCommand);

      sendEntriesToFollowerNodes();
      return this;
    }

    @Override
    public State handle(AppendEntry message) {
      return null;
    }

    @Override
    public State handle(AppendSuccessful message) {
      logger.info("Leader received {}", message);

      final LogEntry logEntry =
          replicatedLog.stream()
              .filter(entry -> entry.getSession() == message.getSession())
              .findFirst()
              .get();

      return maybeCommitEntry(logEntry);
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

    private State appendEntries(AppendEntry message) {
      logger.debug("Follower appended entry: [{}] to replicated log", message::getEntry);
      replicatedLog.add(message.getEntry());

      logger.debug(() -> "Follower send AppendSuccessful");
      final AppendSuccessful response =
          new AppendSuccessful(clusterDiscovery.getSelf(), message.getEntry().getSession());
      send(message.getDiscoveryNode(), response);

      return this;
    }

    @Override
    public RaftState getState() {
      return FOLLOWER;
    }

    @Override
    public State handle(ClientMessage message) {
      return this;
    }

    @Override
    public State handle(AppendEntry message) {
      return appendEntries(message);
    }
  }

  @Override
  protected void doStart() {
  }

  @Override
  protected void doStop() {
  }

  /**
   * Does nothing
   */
  @Override
  protected void doClose() {
  }
}
