package com.github.peshkovm.crdt.replication;

import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.github.peshkovm.crdt.commutative.protocol.DownstreamUpdate;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.netty.NettyTransportService;
import com.github.peshkovm.transport.netty.NettyTransportService.MyChannelFuture;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Default implementation of {@link Replicator}. */
@Component
public class DefaultReplicator extends AbstractLifecycleComponent implements Replicator {

  private final ReentrantLock lock;
  private final ClusterDiscovery clusterDiscovery;
  private final NettyTransportService transportService;
  private Map<DiscoveryNode, ConcurrentLinkedDeque<DownstreamUpdate<?, ?>>> unsentDownstreamUpdates;

  @Autowired
  public DefaultReplicator(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
          ClusterDiscovery clusterDiscovery,
      NettyTransportService transportService) {
    this.lock = new ReentrantLock();
    this.clusterDiscovery = clusterDiscovery;
    this.transportService = transportService;
    this.unsentDownstreamUpdates = new HashMap<>();
  }

  @Override
  public void append(DownstreamUpdate<?, ?> downstreamUpdate) {
    lock.lock();
    try {
      maybeSendEntries(downstreamUpdate);
    } finally {
      lock.unlock();
    }
  }

  private void maybeSendEntries(DownstreamUpdate<?, ?> downstreamUpdate) {
    // send for all replicas
    for (DiscoveryNode node :
        clusterDiscovery.getDiscoveryNodes().remove(clusterDiscovery.getSelf())) {
      final MyChannelFuture<Void> channelFuture = transportService.send(node, downstreamUpdate);

      if (unsentDownstreamUpdates.get(node) == null) {
        unsentDownstreamUpdates.put(node, new ConcurrentLinkedDeque<>());
      }

      if (channelFuture.getFuture().isFailure()) {
        saveUnsentDownstreamUpdates(channelFuture.getDiscoveryNode(), downstreamUpdate);
      } else if (channelFuture.getFuture().isSuccess()) {
        maybeSentSavedDownstreamUpdates(channelFuture.getDiscoveryNode());
      }
    }
  }

  private void maybeSentSavedDownstreamUpdates(DiscoveryNode discoveryNode) {
    unsentDownstreamUpdates
        .get(discoveryNode)
        .forEach(
            downstreamUpdate -> {
              DownstreamUpdate<?, ?> newDownstreamUpdate =
                  new DownstreamUpdate(
                      downstreamUpdate.getCrdtId(),
                      downstreamUpdate.getAtSourceResult(),
                      downstreamUpdate.getCrdtType(),
                      downstreamUpdate.getArgument(),
                      System.nanoTime());
              transportService.send(discoveryNode, newDownstreamUpdate);
              unsentDownstreamUpdates.get(discoveryNode).remove(downstreamUpdate);
            });
  }

  private void saveUnsentDownstreamUpdates(
      DiscoveryNode discoveryNode, DownstreamUpdate<?, ?> downstreamUpdate) {
    unsentDownstreamUpdates.get(discoveryNode).add(downstreamUpdate);
  }

  @Override
  protected void doStart() {}

  @Override
  protected void doStop() {}

  @Override
  protected void doClose() {}
}
