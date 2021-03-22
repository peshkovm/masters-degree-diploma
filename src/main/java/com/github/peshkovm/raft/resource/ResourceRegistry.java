package com.github.peshkovm.raft.resource;

import com.github.peshkovm.common.codec.Message;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ResourceRegistry {

  private static final Logger logger = LogManager.getLogger();
  private volatile Map<Class, ResourceHandler> handlers = HashMap.empty();

  public synchronized <T extends Message> void registerHandler(
      Class<T> type, ResourceHandler<T> handler) {
    handlers = handlers.put(type, handler);
  }

  @SuppressWarnings("unchecked")
  public Message apply(Message message) {
    ResourceHandler handler = handlers.getOrElse(message.getClass(), null);
    if (handler != null) {
      return handler.apply(message);
    } else {
      logger.warn("ResourceHandler not found for: {}", message);
      return null;
    }
  }

  @FunctionalInterface
  public interface ResourceHandler<T extends Message> {

    Message apply(T event);
  }
}
