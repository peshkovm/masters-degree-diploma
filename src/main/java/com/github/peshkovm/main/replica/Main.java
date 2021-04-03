package com.github.peshkovm.main.replica;

import com.github.peshkovm.node.ExternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

  protected static final Logger logger = LogManager.getLogger();

  public static void main(String[] args) {
    InternalNode internalNode = null;
    try {
      internalNode = ExternalClusterFactory.getInternalNode("192.168.0.112", 8801);

      internalNode.start();

      final CountDownLatch countDownLatch = new CountDownLatch(1);
      InternalNode finalInternalNode = internalNode;
      Runtime.getRuntime().addShutdownHook(new Thread(countDownLatch::countDown));
      countDownLatch.await();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (internalNode != null) {
        internalNode.stop();
        internalNode.close();
      }
    }
  }
}
