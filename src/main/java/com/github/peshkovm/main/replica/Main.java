package com.github.peshkovm.main.replica;

import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

  protected static final Logger logger = LogManager.getLogger();

  public static void main(String[] args) throws Exception {
    InternalNode internalNode = null;
    try {
      internalNode = InternalClusterFactory.createInternalNode();

      internalNode.start();

    } catch (Exception e) {
      Objects.requireNonNull(internalNode);

      internalNode.stop();
      internalNode.close();

      e.printStackTrace();
    }
  }
}
