package com.github.peshkovm.crdt.basic;

import com.github.peshkovm.crdt.Crdt;

public interface RegisterCRDT extends Crdt<Long, Long> {
  void assign(Long value);
}
