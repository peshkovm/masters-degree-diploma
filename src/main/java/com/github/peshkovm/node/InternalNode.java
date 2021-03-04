package com.github.peshkovm.node;

import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.github.peshkovm.common.config.ConfigBuilder;
import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;

/**
 * Default implementation of {@link Node} interface.
 */
public class InternalNode extends AbstractLifecycleComponent implements Node {

  private Config config;

  /**
   * Initializes a newly created {@code InternalNode} object with {@link Config} instance containing
   * fields from application.conf file
   */
  public InternalNode() {
    this(new ConfigBuilder().build());
  }

  public InternalNode(Config config) {
    logger.info("Initializing...");

    logger.info("Initialized");
  }

  public void setConfig(Config config) {
    this.config = config;
  }

  public HostAndPort getHostAndPort() {
    return HostAndPort.fromParts(
        config.getString("transport.host"), config.getInt("transport.port"));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InternalNode)) {
      return false;
    }

    InternalNode that = (InternalNode) o;

    return getHostAndPort() != null
        ? getHostAndPort().equals(that.getHostAndPort())
        : that.getHostAndPort() == null;
  }

  @Override
  public int hashCode() {
    return getHostAndPort() != null ? getHostAndPort().hashCode() : 0;
  }

  @Override
  protected void doStart() {
  }

  @Override
  protected void doStop() {
  }

  @Override
  protected void doClose() {
  }
}
