package com.github.peshkovm.common.component;

import com.github.peshkovm.crdt.routing.fsm.Resource;
import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.properties.TypesafePropertySourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import reactor.core.publisher.Sinks;

/** Spring Configuration class for common beans. */
@Configuration
@ComponentScan("com.github.peshkovm")
@PropertySource(
    name = "application.conf",
    factory = TypesafePropertySourceFactory.class,
    value = "classpath:application.conf")
public class ComponentConfiguration {}
