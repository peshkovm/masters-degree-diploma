package com.github.peshkovm.crdt.registry;

import com.github.peshkovm.crdt.Crdt;
import com.github.peshkovm.crdt.commutative.GCounterCmRDT;
import com.github.peshkovm.crdt.replication.Replicator;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultCrdtRegistry implements CrdtRegistry {

  private final Replicator replicator;
  private Map<String, Crdt<?, ?>> crdtMap = HashMap.empty();
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
      crdtMap = crdtMap.put(resourceId, new GCounterCmRDT(resourceId, replicator));
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
