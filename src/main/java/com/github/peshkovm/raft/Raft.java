package com.github.peshkovm.raft;

import com.github.peshkovm.common.Match;
import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.raft.protocol.AppendMessage;
import com.github.peshkovm.raft.protocol.AppendSuccessful;
import com.github.peshkovm.raft.protocol.ClientMessage;
import com.github.peshkovm.raft.resource.ResourceRegistry;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportController;
import com.github.peshkovm.transport.TransportService;
import io.vavr.concurrent.Future;
import io.vavr.concurrent.Promise;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
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
  private final Map<Long, Promise<Message>> sessionCommands;
  private final Map<Long, AtomicInteger> sessionReceives;
  private SourceState sourceState;
  private ReplicaState replicaState;
  private final ResourceRegistry registry;

  private final Match.Mapper<Message, State> mapper =
      Match.<Message, State>map()
          .when(ClientMessage.class, message -> sourceState.handle(message))
          .when(AppendMessage.class, message -> replicaState.handle(message))
          .when(AppendSuccessful.class, message -> sourceState.handle(message))
          .build();

  @Autowired
  public Raft(
      TransportService transportService,
      ClusterDiscovery clusterDiscovery,
      TransportController transportController,
      ResourceRegistry registry) {
    this.transportService = transportService;
    this.clusterDiscovery = clusterDiscovery;
    this.transportController = transportController;
    this.lock = new ReentrantLock();
    this.sessionCommands = new ConcurrentHashMap<>();
    this.sessionReceives = new ConcurrentHashMap<>();
    this.registry = registry;

    //    final RaftMetadata meta = new RaftMetadata(clusterDiscovery);
    //    this.sourceState = new SourceState(meta);
    //    this.replicaState = new ReplicaState(meta);
  }

  public Future<Message> command(Message command) {
    final Promise<Message> promise = Promise.make();
    Promise<Message> prev;
    long session;

    do {
      session = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
      prev = sessionCommands.putIfAbsent(session, promise);
    } while (prev != null);

    sessionReceives.putIfAbsent(
        session, new AtomicInteger(clusterDiscovery.getDiscoveryNodes().size() - 1));

    long finalSession = session;
    final ClientMessage clientMessage = new ClientMessage(command, session);

    logger.info("Created client command: {}", () -> clientMessage);

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
      sendMessageToAllReplicas(message);
      return this;
    }

    public State handle(AppendSuccessful message) {
      logger.info("Source node received {}", message);

      return maybeCommitMessage(message);
    }

    private void sendMessageToAllReplicas(ClientMessage message) {
      logger.info(
          "Source node sending client command to replicas: {}",
          () -> getMeta().getDiscoveryNodesWithout(clusterDiscovery.getSelf()));

      getMeta()
          .getDiscoveryNodesWithout(clusterDiscovery.getSelf())
          .forEach(replica -> sendMessage(replica, message));
    }

    private void sendMessage(DiscoveryNode replica, ClientMessage message) {
      AppendMessage append = new AppendMessage(clusterDiscovery.getSelf(), message);
      send(replica, append);
    }

    private State maybeCommitMessage(AppendSuccessful message) {
      final long session = message.getMessage().getMessage().getSession();
      final Message command = message.getMessage().getMessage().getCommand();

      final Promise<Message> promise = sessionCommands.get(session);

      if (promise != null) {
        final int countOfSessionReceived = sessionReceives.get(session).decrementAndGet();
        if (countOfSessionReceived == 0) {
          logger.info(() -> "Source node received AppendSuccessful from all replicas");
          sessionCommands.remove(session);
          sessionReceives.remove(session);

          final Message result = registry.apply(command);
          if (result != null) {
            promise.success(result);
          } else {
            logger.error("Resource registry not found for {}", () -> command);
            promise.failure(
                new IllegalStateException("Resource registry not found for " + command));
          }
        }
      } else {
        logger.error("Unknown session. Source node can't commit message: " + message);
      }

      return this;
    }
  }

  private class ReplicaState extends State {

    private ReplicaState(RaftMetadata meta) {
      super(meta);
    }

    public State handle(AppendMessage message) {
      return appendMessage(message);
    }

    private State appendMessage(AppendMessage message) {
      logger.info("Replica {} send AppendSuccessful", clusterDiscovery::getSelf);
      final AppendSuccessful response = new AppendSuccessful(clusterDiscovery.getSelf(), message);
      send(message.getDiscoveryNode(), response);

      return this;
    }
  }

  @Override
  protected void doStart() {
    final RaftMetadata meta = new RaftMetadata(clusterDiscovery);
    this.sourceState = new SourceState(meta);
    this.replicaState = new ReplicaState(meta);
  }

  @Override
  protected void doStop() {
  }

  @Override
  protected void doClose() {
  }
}
