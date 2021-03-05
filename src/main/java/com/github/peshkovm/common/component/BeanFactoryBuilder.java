package com.github.peshkovm.common.component;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Builder class to create {@link BeanFactory} from multiple configurations.
 */
public class BeanFactoryBuilder {

  private final AnnotationConfigApplicationContext ctx;

  /**
   * Creates empty BeanFactoryBuilder.
   */
  public BeanFactoryBuilder() {
    ctx = new AnnotationConfigApplicationContext();
  }

  /**
   * Populates BeanFactory with {@code configurationClass} argument.
   *
   * @param configurationClass Spring Configuration class to add to BeanFactory
   * @return populated BeanFactoryBuilder
   */
  public BeanFactoryBuilder add(Class<?> configurationClass) {
    ctx.register(configurationClass);
    return this;
  }

  /**
   * Creates BeanFactory populated with every configuration class added by {@link
   * BeanFactoryBuilder#add(Class)} method.
   *
   * @return populated BeanFactory
   */
  public BeanFactory createBeanFactory() {
    ctx.refresh();
    return ctx;
  }
}
