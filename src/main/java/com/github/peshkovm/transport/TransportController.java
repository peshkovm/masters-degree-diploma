package com.github.peshkovm.transport;

import com.github.peshkovm.common.codec.Message;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Registers {@link Message}-{@link TransportHandler} pairs and dispatches messages to registered
 * handlers.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TransportController {

  private static final Logger logger = LogManager.getLogger();
  private volatile Map<Class, TransportHandler> handlerMap = new ConcurrentHashMap<>();

  /**
   * Registers message's handler.
   *
   * @param requestClass the {@link Class} of message
   * @param handler the handler that will be handling messages
   * @param <T> the type of the message
   */
  public synchronized <T extends Message> void registerMessageHandler(
      Class<T> requestClass, TransportHandler<T> handler) {
    handlerMap.put(requestClass, handler);
  }

  /**
   * Dispatches message to registered handler.
   *
   * @param message message t0 dispatch
   */
  public void dispatch(Message message) {
    final TransportHandler handler = handlerMap.getOrDefault(message.getClass(), null);

    if (handler != null) {
      handler.handle(message);
    } else {
      logger.error("Handler not found for {}", message.getClass());
    }
  }
}
