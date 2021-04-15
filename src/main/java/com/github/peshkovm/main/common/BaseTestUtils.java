package com.github.peshkovm.main.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseTestUtils {

  private static final int numOfCores = Runtime.getRuntime().availableProcessors();

  protected final Logger logger = LogManager.getLogger();
  private final ExecutorService executorService = Executors.newCachedThreadPool();

  protected void executeConcurrently(ThreadTask task) throws Exception {
    final Collection<Callable<Void>> tasks = new ArrayList<>();
    for (int i = 0; i < numOfCores; i++) {
      final int threadNum = i;
      tasks.add(
          () -> {
            task.execute(threadNum, numOfCores);
            return null;
          });
    }
    final List<Future<Void>> futures = executorService.invokeAll(tasks);
    for (Future<Void> future : futures) {
      future.get();
    }
  }

  protected interface ThreadTask {

    void execute(int threadNum, int numOfCores) throws Exception;
  }
}
