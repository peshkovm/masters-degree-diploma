package com.github.peshkovm.crdt.replication;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.netty.NettyTransportService;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Default implementation of {@link Replicator}. */
@Component
public class DefaultReplicator extends AbstractLifecycleComponent implements Replicator {

  private final ReentrantLock lock;
  private final ClusterDiscovery clusterDiscovery;
  private final NettyTransportService transportService;

  @Autowired
  public DefaultReplicator(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
          ClusterDiscovery clusterDiscovery,
      NettyTransportService transportService) {
    this.lock = new ReentrantLock();
    this.clusterDiscovery = clusterDiscovery;
    this.transportService = transportService;
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
    clusterDiscovery
        .getDiscoveryNodes()
        .remove(clusterDiscovery.getSelf())
        .forEach(node -> transportService.send(node, message)); // send for all replicas
  }

  @Override
  protected void doStart() {}

  @Override
  protected void doStop() {}

  @Override
  protected void doClose() {}
}
