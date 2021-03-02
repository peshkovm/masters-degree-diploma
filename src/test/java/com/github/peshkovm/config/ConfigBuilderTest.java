package com.github.peshkovm.config;

import com.github.peshkovm.common.ConfigBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConfigBuilderTest {

  @Test
  @DisplayName("Should parse application.conf")
  void shouldParseApplicationConf() {
    final Config configToTest = new ConfigBuilder().build();
    final Config workingConfig = ConfigFactory.parseResources("application.conf").resolve();

    workingConfig
        .entrySet()
        .forEach(
            field -> {
              final String key = field.getKey();
              final ConfigValue value = field.getValue();

              Assertions.assertEquals(configToTest.getString(key), value.unwrapped().toString());
            });
  }

  @Test
  @DisplayName("Should add fields to config")
  void shouldAddFieldsToConfig() {
    final Config configToTest =
        new ConfigBuilder().with("test.key1", "value1").with("test.key2", "value2").build();
    final Config workingConfig = ConfigFactory.parseResources("application.conf").resolve();

    workingConfig
        .entrySet()
        .forEach(
            field -> {
              final String key = field.getKey();
              final ConfigValue value = field.getValue();

              Assertions.assertEquals(configToTest.getString(key), value.unwrapped().toString());
            });

    Assertions.assertEquals(configToTest.getString("test.key1"), "value1");
    Assertions.assertEquals(configToTest.getString("test.key2"), "value2");
  }

  @Test
  @DisplayName("Should replace value of existing key")
  void shouldReplaceValueOfExistingKey() {
    final Config configToTest =
        new ConfigBuilder()
            .with("transport.host", "127.0.0.123")
            .with("transport.port", "1234")
            .with("transport.max_connections", "55")
            .build();
    final Config workingConfig = ConfigFactory.parseResources("application.conf").resolve();

    workingConfig.entrySet().stream()
        .filter(field -> !field.getKey().equals("transport.host"))
        .filter(field -> !field.getKey().equals("transport.port"))
        .filter(field -> !field.getKey().equals("transport.max_connections"))
        .forEach(
            field -> {
              final String key = field.getKey();
              final ConfigValue value = field.getValue();

              Assertions.assertEquals(configToTest.getString(key), value.unwrapped().toString());
            });

    Assertions.assertEquals(configToTest.getString("transport.host"), "127.0.0.123");
    Assertions.assertEquals(configToTest.getString("transport.port"), "1234");
    Assertions.assertEquals(configToTest.getString("transport.max_connections"), "55");
  }
}
