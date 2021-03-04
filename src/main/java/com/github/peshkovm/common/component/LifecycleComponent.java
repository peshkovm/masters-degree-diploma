package com.github.peshkovm.common.component;

/**
 * Defines methods to control component's life cycle
 */
public interface LifecycleComponent {

  /**
   * Starts component
   */
  void start();

  /**
   * Returns true if component is started, otherwise returns false.
   *
   * @return true if component is started, false otherwise
   */
  boolean isStarted();

  /**
   * Stops component
   */
  void stop();

  /**
   * Returns true if component is stopped, otherwise returns false.
   *
   * @return true if component is stopped, false otherwise
   */
  boolean isStopped();

  /**
   * Closes component
   */
  void close();

  /**
   * Returns true if component is closed, otherwise returns false.
   *
   * @return true if component is closed, false otherwise
   */
  boolean isClosed();
}
