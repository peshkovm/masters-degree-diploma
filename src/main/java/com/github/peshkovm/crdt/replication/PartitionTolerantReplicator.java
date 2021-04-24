package com.github.peshkovm.crdt.replication;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.netty.NettyTransportService;
import com.github.peshkovm.transport.netty.NettyTransportService.DiscoveryFuture;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Default implementation of {@link Replicator}. */
@Component
@Profile("dev")
public class PartitionTolerantReplicator extends AbstractLifecycleComponent implements Replicator {

  private final ReentrantLock lock;
  private final ClusterDiscovery clusterDiscovery;
  private final NettyTransportService transportService;
  private Map<DiscoveryNode, ConcurrentLinkedDeque<Message>> unsentMessages;

  @Autowired
  public PartitionTolerantReplicator(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
          ClusterDiscovery clusterDiscovery,
      NettyTransportService transportService) {
    this.lock = new ReentrantLock();
    this.clusterDiscovery = clusterDiscovery;
    this.transportService = transportService;
    this.unsentMessages = HashMap.empty();
  }

  @Override
  public void replicate(Message message) {
    lock.lock();
    try {
      maybeSendEntries(message);
    } finally {
      lock.unlock();
    }
  }

  private void maybeSendEntries(Message message) {
    // send for all replicas
    for (DiscoveryNode node :
        clusterDiscovery.getDiscoveryNodes().remove(clusterDiscovery.getSelf())) {
      final DiscoveryFuture discoveryFuture = transportService.send(node, message);

      if (unsentMessages.get(node).getOrNull() == null) {
        unsentMessages = unsentMessages.put(node, new ConcurrentLinkedDeque<>());
      }

      if (discoveryFuture.getFuture().isFailure()) {
        saveUnsentDownstreamUpdates(discoveryFuture.getDiscoveryNode(), message);
      } else if (discoveryFuture.getFuture().isSuccess()) {
        maybeSentSavedDownstreamUpdates(discoveryFuture.getDiscoveryNode());
      }
    }
  }

  private void maybeSentSavedDownstreamUpdates(DiscoveryNode discoveryNode) {
    unsentMessages
        .get(discoveryNode)
        .get()
        .forEach(
            downstreamUpdate -> {
              transportService.send(discoveryNode, downstreamUpdate);
              unsentMessages.get(discoveryNode).get().remove(downstreamUpdate);
            });
  }

  private void saveUnsentDownstreamUpdates(DiscoveryNode discoveryNode, Message message) {
    unsentMessages.get(discoveryNode).get().add(message);
  }

  @Override
  protected void doStart() {}

  @Override
  protected void doStop() {}

  @Override
  protected void doClose() {}
}
