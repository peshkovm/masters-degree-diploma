package com.github.peshkovm.crdt.basic;

import com.github.peshkovm.crdt.Crdt;
import java.io.Serializable;

/**
 * A register is a memory cell storing an opaque atom or object (noted type X hereafter). It
 * supports assign to update its value, and value to query it. Non-concurrent assigns preserve
 * sequential semantics: the later one overwrites the earlier one. Unless safeguards are taken,
 * concurrent updates do not commute.
 */
public interface RegisterCRDT<T extends Serializable, R extends Serializable> extends Crdt<T, R> {

  /**
   * Updates register's value.
   *
   * @param value
   */
  void assign(T value);

  /**
   * Returns register's value.
   *
   * @return
   */
  R value();
}
