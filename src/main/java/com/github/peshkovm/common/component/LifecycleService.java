package com.github.peshkovm.common.component;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluate lifecycle methods on all components implementing {@link LifecycleComponent}.
 */
public class LifecycleService extends AbstractLifecycleComponent {

  private final List<LifecycleComponent> lifecycleQueue = new ArrayList<>();

  @Override
  protected void doStart() {
    lifecycleQueue.forEach(
        component -> {
          logger.debug("Starting{}", component);
          component.start();
        });
  }

  @Override
  protected void doStop() {
    final int size = lifecycleQueue.size();
    for (int i = size - 1; i >= 0; i--) {
      LifecycleComponent component = lifecycleQueue.get(i);
      logger.debug("Stopping {}", component);
      component.stop();
    }
  }

  @Override
  protected void doClose() {
    final int size = lifecycleQueue.size();
    for (int i = size - 1; i >= 0; i--) {
      LifecycleComponent component = lifecycleQueue.get(i);
      logger.debug("Closing {}", component);
      component.close();
    }
  }
}
