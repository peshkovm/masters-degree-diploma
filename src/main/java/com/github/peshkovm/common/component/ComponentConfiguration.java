package com.github.peshkovm.common.component;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/** Spring Configuration class for common beans. */
@Configuration
public class ComponentConfiguration {

  // Instantiated once per ApplicationContext
  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  public LifecycleService lifecycleService() {
    return new LifecycleService();
  }
}
