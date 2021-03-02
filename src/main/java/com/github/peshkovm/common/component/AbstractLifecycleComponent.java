package com.github.peshkovm.common.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default implementation of {@link LifecycleComponent}.
 */
public abstract class AbstractLifecycleComponent implements LifecycleComponent {

  protected final Logger logger = LogManager.getLogger(getClass());
  private final Lifecycle lifecycle = new Lifecycle();

  @Override
  public void start() {
    if (lifecycle.canMoveToStarted()) {
      lifecycle.moveToStarted();

      logger.info("Starting");
      doStart();
      logger.info("Started");
    } else {
      logger.warn("Can't move to started from " + lifecycle.getState() + " state");
    }
  }

  /**
   * Component's starting logic
   */
  protected abstract void doStart();

  @Override
  public void stop() {
    if (lifecycle.canMoveToStopped()) {
      lifecycle.moveToStopped();

      logger.info("Stopping");
      doStop();
      logger.info("Stopped");
    } else {
      logger.warn("Can't move to stopped from " + lifecycle.getState() + " state");
    }
  }

  /**
   * Component's stopping logic
   */
  protected abstract void doStop();

  @Override
  public void close() {
    if (lifecycle.canMoveToClosed()) {
      lifecycle.moveToClosed();

      logger.info("Closing");
      doClose();
      logger.info("Closed");
    } else {
      logger.warn("Can't move to closed from " + lifecycle.getState() + " state");
    }
  }

  /**
   * Component's clossing logic
   */
  protected abstract void doClose();
}
