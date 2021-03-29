package com.github.peshkovm.common;

import java.io.Serializable;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

// note: class name need not match the @Plugin name.
@Plugin(name = "ThrowingAppender", category = "Core", elementType = "appender", printObject = true)
public final class ThrowingAppender extends AbstractAppender {

  private final boolean shouldThrow;

  protected ThrowingAppender(
      String name,
      Filter filter,
      Layout<? extends Serializable> layout,
      boolean ignoreExceptions,
      boolean shouldExitOnError,
      Property[] properties) {
    super(name, filter, layout, ignoreExceptions, properties);
    this.shouldThrow = shouldExitOnError;
  }

  // The append method is where the appender does the work.
  // Given a log event, you are free to do with it what you want.
  // This example demonstrates:
  // 1. Concurrency: this method may be called by multiple threads concurrently
  // 2. How to use layouts
  // 3. Error handling
  @Override
  public void append(LogEvent event) {
    if (shouldThrow) {
      if (event.getLevel().isLessSpecificThan(Level.WARN)) {
        writeToConsole(event);
      } else {
        if (!ignoreExceptions()) {
          //          try {
          //            throw new IllegalStateException(event.getMessage().getFormattedMessage());
          //          } catch (IllegalStateException e) {
          //            LOGGER.error(e);
          //          }
          writeToConsole(event);
          System.exit(1);
        }
      }
    } else {
      writeToConsole(event);
    }
  }

  private void writeToConsole(LogEvent event) {
    try {
      final byte[] bytes = getLayout().toByteArray(event);
      System.out.write(bytes);
      System.out.flush();
    } catch (Exception ex) {
      if (!ignoreExceptions()) {
        throw new AppenderLoggingException(ex);
      }
    }
  }

  // Your custom appender needs to declare a factory method
  // annotated with `@PluginFactory`. Log4j will parse the configuration
  // and call this factory method to construct an appender instance with
  // the configured attributes.
  @PluginFactory
  public static ThrowingAppender createAppender(
      @PluginAttribute("name") String name,
      @PluginElement("Layout") Layout<? extends Serializable> layout,
      @PluginElement("Filter") final Filter filter,
      @PluginAttribute("shouldExitOnError") boolean shouldExitOnError) {
    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new ThrowingAppender(name, filter, layout, false, shouldExitOnError, null);
  }
}
