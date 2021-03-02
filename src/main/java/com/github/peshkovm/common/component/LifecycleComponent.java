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
   * Stops component
   */
  void stop();

  /**
   * Closes component
   */
  void close();
}
