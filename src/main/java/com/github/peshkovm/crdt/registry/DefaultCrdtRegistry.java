package com.github.peshkovm.crdt.registry;

import com.github.peshkovm.crdt.Crdt;
import com.github.peshkovm.crdt.commutative.GCounter;
import com.github.peshkovm.crdt.replication.Replicator;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultCrdtRegistry implements CrdtRegistry {

  private final Replicator replicator;
  private Map<String, Crdt> crdtMap = HashMap.empty();
  private final ReentrantLock lock;

  @Autowired
  public DefaultCrdtRegistry(Replicator replicator) {
    this.lock = new ReentrantLock();
    this.replicator = replicator;
  }

  @Override
  public boolean createGCounter(String resourceId) {
    lock.lock();
    try {
      if (crdtMap.containsKey(resourceId)) {
        return false;
      }
      crdtMap = crdtMap.put(resourceId, new GCounter(resourceId, replicator));
      return true;
    } finally {
      lock.unlock();
    }
  }
}
