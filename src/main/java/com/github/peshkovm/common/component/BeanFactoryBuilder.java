package com.github.peshkovm.common.component;

import java.util.function.Supplier;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/** Builder class to create {@link BeanFactory} from multiple configurations. */
public class BeanFactoryBuilder {
  private final AnnotationConfigApplicationContext ctx;

  /** Creates empty BeanFactoryBuilder. */
  public BeanFactoryBuilder() {
    ctx = new AnnotationConfigApplicationContext();
  }

  /**
   * Populates BeanFactory with {@code configurationClass}.
   *
   * @param configurationClass Spring Configuration class to add to BeanFactory
   * @return populated BeanFactoryBuilder
   */
  public BeanFactoryBuilder add(Class<?> configurationClass) {
    ctx.register(configurationClass);

    return this;
  }

  /**
   * Populates BeanFactory with {@code bean} instance.
   *
   * @param bean instance of the bean to add to BeanFactory
   * @param <T> Type of bean
   * @return populated BeanFactoryBuilder
   */
  public <T> BeanFactoryBuilder addBean(T bean) {
    addBean(bean, bd -> {});
    return this;
  }

  /**
   * Populates BeanFactory with {@code bean} instance and customize the given bean with {@code
   * beanDefinitionCustomizer} callback.
   *
   * @param bean instance of the bean to add to BeanFactory
   * @param beanDefinitionCustomizer Callback for customizing the given bean definition
   * @param <T> Type of bean
   * @return populated BeanFactoryBuilder
   */
  public <T> BeanFactoryBuilder addBean(T bean, BeanDefinitionCustomizer beanDefinitionCustomizer) {
    ctx.registerBean(bean.getClass(), (Supplier) () -> bean, beanDefinitionCustomizer);
    return this;
  }

  /**
   * Populates BeanFactory with {@code bean} instance and customize the given bean with {@code
   * beanDefinitionCustomizer} callback.
   *
   * @param beanName the name of the bean (may be null)
   * @param bean instance of the bean to add to BeanFactory
   * @param beanDefinitionCustomizer Callback for customizing the given bean definition
   * @param <T> Type of bean
   * @return populated BeanFactoryBuilder
   */
  public <T> BeanFactoryBuilder addBean(
      String beanName, T bean, BeanDefinitionCustomizer beanDefinitionCustomizer) {
    ctx.registerBean(beanName, bean.getClass(), (Supplier) () -> bean, beanDefinitionCustomizer);
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
