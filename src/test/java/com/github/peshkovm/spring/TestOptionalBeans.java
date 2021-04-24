package com.github.peshkovm.spring;

import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestOptionalBeans {

  private InternalNode internalNode;

  @BeforeEach
  void setUp() {
    internalNode = InternalClusterFactory.createInternalNode(new OptionalBean());
  }

  @Test
  @DisplayName("Should register optional bean")
  void shouldRegisterOptionalBean() {
    final OptionalBean bean = internalNode.getBeanFactory().getBean(OptionalBean.class);

    Assertions.assertTrue(bean.isCreated);
  }

  @Data
  public static class OptionalBean {
    private final boolean isCreated;

    public OptionalBean() {
      isCreated = true;
    }
  }

  @AfterEach
  void tearDown() {
    InternalClusterFactory.reset();
  }
}
