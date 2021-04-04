package com.github.peshkovm.raft;

import com.github.peshkovm.common.Match;
import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.raft.protocol.ClientCommand;
import com.github.peshkovm.raft.protocol.ClientMessage;
import com.github.peshkovm.raft.protocol.ClientMessageFailure;
import com.github.peshkovm.raft.protocol.ClientMessageSuccessful;
import com.github.peshkovm.raft.protocol.CommandResult;
import com.github.peshkovm.raft.resource.ResourceRegistry;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportController;
import com.github.peshkovm.transport.TransportService;
import io.vavr.concurrent.Future;
import io.vavr.concurrent.Promise;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
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
  private final Map<Long, Promise<ConcurrentLinkedDeque<CommandResult>>> sessionCommands;
  private final Map<Long, AtomicInteger> sessionReceives;
  private final Map<Long, ConcurrentLinkedDeque<CommandResult>> sessionResults;
  private SourceState sourceState;
  private ReplicaState replicaState;
  private final ResourceRegistry registry;

  private final Match.Mapper<Message> mapper =
      Match.<Message>map()
          .when(ClientCommand.class, message -> sourceState.handle(message))
          .when(ClientMessage.class, message -> replicaState.handle(message))
          .when(ClientMessageSuccessful.class, message -> sourceState.handle(message))
          .when(ClientMessageFailure.class, message -> sourceState.handle(message))
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
    this.sessionResults = new ConcurrentHashMap<>();
    this.registry = registry;
  }

  public Future<ConcurrentLinkedDeque<CommandResult>> command(Message command) {
    final Promise<ConcurrentLinkedDeque<CommandResult>> promise = Promise.make();
    Promise<ConcurrentLinkedDeque<CommandResult>> prev;
    long session;

    do {
      session = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
      prev = sessionCommands.putIfAbsent(session, promise);
    } while (prev != null);

    sessionReceives.putIfAbsent(
        session, new AtomicInteger(clusterDiscovery.getDiscoveryNodes().size() - 1));
    sessionResults.put(session, new ConcurrentLinkedDeque<>());

    final ClientCommand clientMessage = new ClientCommand(command, session);

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

    public void handle(ClientCommand message) {
      sendMessageToAllReplicas(message);
    }

    public void handle(ClientMessageSuccessful message) {
      logger.info("Source node received {}", message);

      maybeCommitMessage(message);
    }

    public void handle(ClientMessageFailure message) {
      final long session = message.getClientCommand().getSession();
      final Message command = message.getClientCommand().getCommand();
      final CommandResult commandResult = message.getCommandResult();

      final Promise<ConcurrentLinkedDeque<CommandResult>> promise = sessionCommands.get(session);
      int countOfSessionsToReceive = sessionReceives.get(session).getAndSet(0);

      if (countOfSessionsToReceive != 0) {
        if (promise != null) {
          sessionCommands.remove(session);
          promise.failure(new IllegalStateException("Command result is failure for " + command));
        } else {
          logger.error("Unknown session. Source node can't commit message: " + message);
        }
      } else {
        logger.info("Source already received ClientMessageFailure. Ignoring this message");
      }
    }

    private void sendMessageToAllReplicas(ClientCommand message) {
      logger.info(
          "Source node sending client command to replicas: {}",
          () -> getMeta().getDiscoveryNodesWithout(clusterDiscovery.getSelf()));

      getMeta()
          .getDiscoveryNodesWithout(clusterDiscovery.getSelf())
          .forEach(replica -> sendMessage(replica, message));
    }

    private void sendMessage(DiscoveryNode replica, ClientCommand message) {
      ClientMessage append = new ClientMessage(clusterDiscovery.getSelf(), message);
      send(replica, append);
    }

    private void maybeCommitMessage(ClientMessageSuccessful message) {
      final long session = message.getClientCommand().getSession();
      final Message command = message.getClientCommand().getCommand();
      final CommandResult replicaCommandResult = message.getCommandResult();

      final Promise<ConcurrentLinkedDeque<CommandResult>> promise = sessionCommands.get(session);

      if (promise != null) {
        sessionResults.get(session).add(replicaCommandResult);
        final int countOfSessionsToReceive = sessionReceives.get(session).decrementAndGet();

        if (countOfSessionsToReceive == 0) {
          logger.info(() -> "Source node received AppendSuccessful from all replicas");
          sessionCommands.remove(session);
          sessionReceives.remove(session);

          final CommandResult sourceCommandResult = registry.apply(command);
          sessionResults.get(session).add(sourceCommandResult);
          if (sourceCommandResult.isSuccessful()) {
            promise.success(sessionResults.get(session));
          } else {
            logger.error("Command result is failure for {}", () -> command);
            promise.failure(new IllegalStateException("Command result is failure for " + command));
          }
        }
      } else {
        logger.error("Unknown session. Source node can't commit message: " + message);
      }
    }
  }

  private class ReplicaState extends State {

    private ReplicaState(RaftMetadata meta) {
      super(meta);
    }

    private void handle(ClientMessage clientMessage) {
      final CommandResult commandResult;
      final ClientCommand clientCommand = clientMessage.getMessage();
      final Message response;

      commandResult = registry.apply(clientMessage.getMessage().getCommand());

      if (commandResult.isSuccessful()) {
        response =
            new ClientMessageSuccessful(clusterDiscovery.getSelf(), clientCommand, commandResult);
      } else {
        response =
            new ClientMessageFailure(clusterDiscovery.getSelf(), clientCommand, commandResult);
      }

      send(clientMessage.getDiscoveryNode(), response);
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
