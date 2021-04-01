package com.github.peshkovm.spring;

import com.github.peshkovm.common.component.BeanFactoryBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class LifecycleServiceTest {

  private AnnotationConfigApplicationContext context;

  @AfterEach
  void tearDown() {
    context.close();
  }

  @Test
  @DisplayName("Should add all registered beans to list")
  void shouldAddAllRegisteredBeansToList() {
    final Set<Object> testBeans = new HashSet<>();
    final BeanFactoryBuilder beanFactoryBuilder = new BeanFactoryBuilder();

    testBeans.add(new LifecycleServiceMock());
    testBeans.add("Test string 1");
    testBeans.add(1);
    testBeans.add(1.1);

    testBeans.forEach(beanFactoryBuilder::addBean);

    this.context = (AnnotationConfigApplicationContext) beanFactoryBuilder.createBeanFactory();
    final Collection<Object> allBeans = context.getBeansOfType(Object.class).values();
    final LifecycleServiceMock lifecycleService = context.getBean(LifecycleServiceMock.class);

    Assertions.assertAll(
        testBeans.stream()
            .map(
                bean ->
                    () ->
                        Assertions.assertTrue(
                            allBeans.contains(bean),
                            "ApplicationContext doesn't contain " + bean)));

    Assertions.assertAll(
        testBeans.stream()
            .map(
                bean ->
                    () -> {
                      if (!(bean instanceof LifecycleServiceMock)) {
                        Assertions.assertTrue(
                            lifecycleService.getLifecycleQueue().contains(bean),
                            "LifecycleService doesn't contain " + bean);
                      }
                    }));
  }

  private static class LifecycleServiceMock implements BeanPostProcessor {
    @Getter private final List<Object> lifecycleQueue = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName)
        throws BeansException {
      lifecycleQueue.add(bean);
      return bean;
    }
  }
}
