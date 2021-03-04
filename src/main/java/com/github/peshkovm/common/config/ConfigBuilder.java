package com.github.peshkovm.common.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

/**
 * Builder class to create {@link Config} instances.
 */
public class ConfigBuilder {

  private Config config = ConfigFactory.empty();

  /**
   * Adds HOCON field with key = {@code path} and value = {@code value}. If the key already has a
   * value, that value is replaced.
   *
   * @param path key of field
   * @param value value of field
   * @return ConfigBuilder based on this one, but with the given key set to the given value.
   */
  public ConfigBuilder with(String path, Object value) {
    this.config = this.config.withValue(path, ConfigValueFactory.fromAnyRef(value));
    return this;
  }

  /**
   * Builds {@link Config} instance containing fields from application.conf file and fields added by
   * {@link ConfigBuilder#with(String, Object)} method. Keys added by {@code with(String, Object)}
   * method "winning" over the application.conf one.
   *
   * @return built Config instance
   */
  public Config build() {
    return config.withFallback(ConfigFactory.parseResources("application.conf").resolve());
  }
}
