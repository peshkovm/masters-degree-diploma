package com.github.peshkovm.crdt.registry;

import com.github.peshkovm.crdt.Crdt;
import com.github.peshkovm.crdt.operationbased.GCounterCmRDT;
import com.github.peshkovm.crdt.operationbased.LWWRegisterCmRDT;
import com.github.peshkovm.crdt.replication.Replicator;
import com.github.peshkovm.crdt.statebased.GCounterCvRDT;
import com.github.peshkovm.crdt.statebased.MVRegisterCvRDT;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Default implementation io {@link CrdtRegistry}. */
@Component
public class DefaultCrdtRegistry implements CrdtRegistry {

  private final Replicator replicator;
  private final ClusterDiscovery discovery;
  private Map<CrdtIdClassPair, Crdt<?, ?>> crdtMap = HashMap.empty();
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
    final CrdtIdClassPair pair = new CrdtIdClassPair(resourceId, GCounterCmRDT.class);
    try {
      if (crdtMap.containsKey(pair)) {
        return false;
      }
      crdtMap = crdtMap.put(pair, new GCounterCmRDT(resourceId, replicator));
      return true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean createLWWRegisterCmRDT(String resourceId) {
    lock.lock();
    final CrdtIdClassPair pair = new CrdtIdClassPair(resourceId, LWWRegisterCmRDT.class);
    try {
      if (crdtMap.containsKey(pair)) {
        return false;
      }
      crdtMap = crdtMap.put(pair, new LWWRegisterCmRDT(resourceId, replicator));
      return true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean createGCounterCvRDT(String resourceId) {
    lock.lock();
    final CrdtIdClassPair pair = new CrdtIdClassPair(resourceId, GCounterCvRDT.class);
    try {
      if (crdtMap.containsKey(pair)) {
        return false;
      }
      final int numOfNodes = discovery.getDiscoveryNodes().length();
      final int id = discovery.getDiscoveryNodes().toList().indexOf(discovery.getSelf());

      crdtMap = crdtMap.put(pair, new GCounterCvRDT(resourceId, numOfNodes, id, replicator));
      return true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean createMVRegisterCvRDT(String resourceId) {
    lock.lock();
    final CrdtIdClassPair pair = new CrdtIdClassPair(resourceId, MVRegisterCvRDT.class);
    try {
      if (crdtMap.containsKey(pair)) {
        return false;
      }
      final int numOfNodes = discovery.getDiscoveryNodes().length();
      final int id = discovery.getDiscoveryNodes().toList().indexOf(discovery.getSelf());

      crdtMap = crdtMap.put(pair, new MVRegisterCvRDT(resourceId, numOfNodes, id, replicator));
      return true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean deleteCRDT(String resourceId, Class<? extends Crdt<?, ?>> resourceClass) {
    lock.lock();
    final CrdtIdClassPair pair = new CrdtIdClassPair(resourceId, resourceClass);
    try {
      if (!crdtMap.containsKey(pair)) {
        return false;
      }
      crdtMap = crdtMap.remove(pair);
      return true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public <T extends Crdt<?, ?>> T crdt(String crdtId, Class<T> crdtClass) {
    final CrdtIdClassPair pair = new CrdtIdClassPair(crdtId, crdtClass);
    final var crdt =
        crdtMap
            .get(pair)
            .getOrElseThrow(
                () -> new IllegalArgumentException("CRDT " + crdtId + " not registered"));

    if (crdtClass.isInstance(crdt)) {
      return (T) crdt;
    } else {
      throw new IllegalArgumentException(
          "Was requested "
              + crdtClass
              + " "
              + crdtId
              + ", but actual is "
              + crdt.getClass().getTypeName()
              + " "
              + crdtId);
    }
  }

  @Data
  private class CrdtIdClassPair {
    private final String id;
    private final Class<? extends Crdt<?, ?>> clazz;

    public CrdtIdClassPair(String id, Class<? extends Crdt<?, ?>> clazz) {
      this.id = id;
      this.clazz = clazz;
    }
  }
}
