package com.github.peshkovm.common.component;

import com.github.peshkovm.common.component.Lifecycle.State;
import com.github.peshkovm.node.InternalNode;
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

      if (this instanceof InternalNode) {
        logger.info("Starting");
      } else {
        logger.debug("Starting");
      }

      doStart();

      if (this instanceof InternalNode) {
        logger.info("Started");
      } else {
        logger.debug("Started");
      }

    } else {
      throw new IllegalStateException(
          "Can't move to started from " + lifecycle.getState() + " state");
    }
  }

  /** Component's starting logic */
  protected abstract void doStart();

  @Override
  public boolean isStarted() {
    return lifecycle.getState() == State.STARTED;
  }

  @Override
  public void stop() {
    if (lifecycle.canMoveToStopped()) {
      lifecycle.moveToStopped();

      if (this instanceof InternalNode) {
        logger.info("Stopping");
      } else {
        logger.debug("Stopping");
      }

      doStop();

      if (this instanceof InternalNode) {
        logger.info("Stopped");
      } else {
        logger.debug("Stopped");
      }

    } else {
      throw new IllegalStateException(
          "Can't move to stopped from " + lifecycle.getState() + " state");
    }
  }

  /** Component's stopping logic */
  protected abstract void doStop();

  @Override
  public boolean isStopped() {
    return lifecycle.getState() == State.STOPPED;
  }

  @Override
  public void close() {
    if (lifecycle.canMoveToClosed()) {
      lifecycle.moveToClosed();

      if (this instanceof InternalNode) {
        logger.info("Closing");
      } else {
        logger.debug("Closing");
      }

      doClose();

      if (this instanceof InternalNode) {
        logger.info("Closed");
      } else {
        logger.debug("Closed");
      }
    } else {
      throw new IllegalStateException(
          "Can't move to closed from " + lifecycle.getState() + " state");
    }
  }

  /** Component's clossing logic */
  protected abstract void doClose();

  @Override
  public boolean isClosed() {
    return lifecycle.getState() == State.CLOSED;
  }
}
