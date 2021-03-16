package com.github.peshkovm.node;

import org.springframework.beans.factory.BeanFactory;

/**
 * Represents cluster node interface.
 */
public interface Node {

  /**
   * Returns node's {@link BeanFactory}.
   *
   * @return node Spring BeanFactory.
   */
  BeanFactory getBeanFactory();
}
