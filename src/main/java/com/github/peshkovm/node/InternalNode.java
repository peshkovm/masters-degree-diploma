package com.github.peshkovm.node;

import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.typesafe.config.Config;

public class InternalNode extends AbstractLifecycleComponent implements Node {

  private final Config config;

  public InternalNode(Config config) {
    this.config = config;

    logger.info("Initializing...");

    logger.info("Initialized");
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
