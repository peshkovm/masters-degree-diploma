package com.github.peshkovm.common.component;

/**
 * Represents life cycle of component. Initial state is {@link State#INITIALIZED INITIALIZED}.
 */
public class Lifecycle {

  public State getState() {
    return state;
  }

  private volatile State state = State.INITIALIZED;

  /**
   * Returns <tt>true</tt> if component can be started. Otherwise returns <tt>false</tt>.
   *
   * @return <tt>true</tt> if can be moved to started, <tt>false</tt> otherwise.
   */
  public boolean canMoveToStarted() {
    final State componentState = this.state;

    return componentState == State.INITIALIZED || componentState == State.STOPPED;
  }

  /**
   * Set component's state to {@link State#STARTED STARTED} if can. Otherwise throw {@code
   * Exception}.
   */
  public void moveToStarted() {
    if (canMoveToStarted()) {
      this.state = State.STARTED;
    } else {
      throw new IllegalStateException("Can't move to started from " + this.state + " state");
    }
  }

  /**
   * Returns <tt>true</tt> if component can be stopped. Otherwise returns <tt>false</tt>.
   *
   * @return <tt>true</tt> if can be moved to stopped, <tt>false</tt> otherwise.
   */
  public boolean canMoveToStopped() {
    final State componentState = this.state;

    return componentState == State.STARTED;
  }

  /**
   * Set component's state to {@link State#STOPPED STOPPED} if can. Otherwise throw {@code
   * Exception}.
   */
  public void moveToStopped() {
    if (canMoveToStopped()) {
      this.state = State.STOPPED;
    } else {
      throw new IllegalStateException("Can't move to stopped from " + this.state + " state");
    }
  }

  /**
   * Returns <tt>true</tt> if component can be closed. Otherwise returns <tt>false</tt>.
   *
   * @return <tt>true</tt> if can be moved to closed, <tt>false</tt> otherwise.
   */
  public boolean canMoveToClosed() {
    final State componentState = this.state;

    return componentState != State.CLOSED;
  }

  /**
   * Set component's state to {@link State#CLOSED CLOSED} if can. Otherwise throw {@code
   * Exception}.
   */
  public void moveToClosed() {
    if (canMoveToClosed()) {
      this.state = State.CLOSED;
    } else {
      throw new IllegalStateException("Can't move to closed from " + this.state + " state");
    }
  }

  /**
   * Components lifecycle state
   */
  public enum State {
    INITIALIZED,
    STOPPED,
    STARTED,
    CLOSED
  }
}
