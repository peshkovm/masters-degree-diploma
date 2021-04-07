package com.github.peshkovm.common.component;

import com.github.peshkovm.common.diagram.DiagramBuilderSingleton;
import com.github.peshkovm.crdt.routing.fsm.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

/**
 * Spring Configuration class for common beans.
 */
@Configuration
@ComponentScan("com.github.peshkovm")
public class ComponentConfiguration {

  @Bean
  public Sinks.Many<Resource> eventBus() {
    return Sinks.many().replay().all();
  }

  @Bean
  public DiagramBuilderSingleton diagramBuilderSingleton() throws Exception {
    return DiagramBuilderSingleton.getInstance();
  }
}
