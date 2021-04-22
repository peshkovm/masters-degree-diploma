package com.github.peshkovm.diagram.properties;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.IOException;
import java.util.Objects;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

public class TypesafePropertySourceFactory implements PropertySourceFactory {

  @Override
  public PropertySource<?> createPropertySource(String name, EncodedResource resource)
      throws IOException {
    Config config =
        ConfigFactory.load(Objects.requireNonNull(resource.getResource().getFilename())).resolve();

    return new TypesafeConfigPropertySource(name, config);
  }
}
