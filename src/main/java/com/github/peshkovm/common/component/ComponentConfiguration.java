package com.github.peshkovm.common.component;

import com.github.peshkovm.crdt.routing.fsm.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

/** Spring Configuration class for common beans. */
@Configuration
@ComponentScan("com.github.peshkovm")
public class ComponentConfiguration {}
