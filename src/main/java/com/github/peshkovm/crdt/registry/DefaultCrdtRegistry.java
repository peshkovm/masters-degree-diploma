package com.github.peshkovm.crdt.registry;

import com.github.peshkovm.crdt.Crdt;
import com.github.peshkovm.crdt.operation.GCounterCmRDT;
import com.github.peshkovm.crdt.replication.Replicator;
import com.github.peshkovm.crdt.state.GCounterCvRDT;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Default implementation io {@link CrdtRegistry}. */
@Component
public class DefaultCrdtRegistry implements CrdtRegistry {

  private final Replicator replicator;
  private final ClusterDiscovery discovery;
  private Map<String, Crdt<?, ?>> crdtMap = HashMap.empty();
  private final ReentrantLock lock;

  @Autowired
  public DefaultCrdtRegistry(Replicator replicator, ClusterDiscovery discovery) {
    this.lock = new ReentrantLock();
    this.replicator = replicator;
    this.discovery = discovery;
  }

  @Override
  public boolean createGCounterCmRDT(String resourceId) {
    lock.lock();
    try {
      if (crdtMap.containsKey(resourceId)) {
        return false;
      }
      crdtMap = crdtMap.put(resourceId, new GCounterCmRDT(resourceId, replicator));
      return true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean createGCounterCvRDT(String resourceId) {
    lock.lock();
    try {
      if (crdtMap.containsKey(resourceId)) {
        return false;
      }
      final int numOfNodes = discovery.getDiscoveryNodes().length();
      final int id = discovery.getDiscoveryNodes().toList().indexOf(discovery.getSelf());

      crdtMap = crdtMap.put(resourceId, new GCounterCvRDT(resourceId, numOfNodes, id, replicator));
      return true;
    } finally {
      lock.unlock();
    }
  }

  public Crdt<?, ?> crdt(String crdtId) {
    var crdt =
        crdtMap
            .get(crdtId)
            .getOrElseThrow(
                () -> new IllegalArgumentException("CRDT " + crdtId + " not registered"));
    return crdt;
  }

  @Override
  public <T extends Crdt<?, ?>> T crdt(String crdtId, Class<T> crdtType) {
    final var crdt = crdt(crdtId);

    if (crdtType.isInstance(crdt)) {
      return (T) crdt;
    } else {
      throw new IllegalArgumentException(
          "Was requested "
              + crdtType
              + " "
              + crdtId
              + ", but actual is "
              + crdt.getClass().getTypeName()
              + " "
              + crdtId);
    }
  }
}
