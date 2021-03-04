package com.github.peshkovm.common.component;

import com.github.peshkovm.common.component.Lifecycle.State;
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

      logger.debug("Starting");
      doStart();
      logger.debug("Started");
    } else {
      logger.warn("Can't move to started from " + lifecycle.getState() + " state");
    }
  }

  /**
   * Component's starting logic
   */
  protected abstract void doStart();

  @Override
  public boolean isStarted() {
    return lifecycle.getState() == State.STARTED;
  }

  @Override
  public void stop() {
    if (lifecycle.canMoveToStopped()) {
      lifecycle.moveToStopped();

      logger.debug("Stopping");
      doStop();
      logger.debug("Stopped");
    } else {
      logger.warn("Can't move to stopped from " + lifecycle.getState() + " state");
    }
  }

  /**
   * Component's stopping logic
   */
  protected abstract void doStop();

  @Override
  public boolean isStopped() {
    return lifecycle.getState() == State.STOPPED;
  }

  @Override
  public void close() {
    if (lifecycle.canMoveToClosed()) {
      lifecycle.moveToClosed();

      logger.debug("Closing");
      doClose();
      logger.debug("Closed");
    } else {
      logger.warn("Can't move to closed from " + lifecycle.getState() + " state");
    }
  }

  /**
   * Component's clossing logic
   */
  protected abstract void doClose();

  @Override
  public boolean isClosed() {
    return lifecycle.getState() == State.CLOSED;
  }
}
