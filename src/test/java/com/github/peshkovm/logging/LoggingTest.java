package com.github.peshkovm.logging;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class LoggingTest {

  private TestInfo testInfo;

  @BeforeEach
  void setUp(TestInfo info) {
    testInfo = info;
  }

  private String getName() {
    return getClass().getSimpleName() + "." + testInfo.getDisplayName();
  }

  private void addAppender(final OutputStream outputStream, final String outputStreamName) {
    final LoggerContext context = LoggerContext.getContext(false);
    final Configuration config = context.getConfiguration();
    final PatternLayout layout = PatternLayout.createDefaultLayout(config);
    final Appender appender =
        OutputStreamAppender.createAppender(
            layout, null, outputStream, outputStreamName, false, true);
    appender.start();
    config.addAppender(appender);
    updateLoggers(appender, config);
  }

  static void updateLoggers(final Appender appender, final Configuration config) {
    final Level level = null;
    final Filter filter = null;
    for (final LoggerConfig loggerConfig : config.getLoggers().values()) {
      loggerConfig.addAppender(appender, level, filter);
    }
    config.getRootLogger().addAppender(appender, level, filter);
  }

  @Test
  @DisplayName("Should print debug log to console")
  void shouldPrintDebugLogToConsole() {
    final OutputStream out = new ByteArrayOutputStream();
    final String name = getName();
    final Logger logger = LogManager.getLogger(name);

    addAppender(out, name);
    logger.info("debug message");
    final String actual = out.toString();

    Assertions.assertTrue(actual.contains("debug message"));
  }

  @Test
  @DisplayName("Should print error log to console")
  void shouldPrintErrorLogToConsole() {
    final OutputStream out = new ByteArrayOutputStream();
    final String name = getName();
    final Logger logger = LogManager.getLogger(name);

    addAppender(out, name);
    logger.error("error message");
    final String actual = out.toString();

    Assertions.assertTrue(actual.contains("error message"));
  }

  @Test
  @DisplayName("Should print info log to console")
  void shouldPrintInfoLogToConsole() {
    final OutputStream out = new ByteArrayOutputStream();
    final String name = getName();
    final Logger logger = LogManager.getLogger(name);

    addAppender(out, name);
    logger.info("info message");
    final String actual = out.toString();

    Assertions.assertTrue(actual.contains("info message"));
  }

  @Test
  @DisplayName("Should print warn log to console")
  void shouldPrintWarnLogToConsole() {
    final OutputStream out = new ByteArrayOutputStream();
    final String name = getName();
    final Logger logger = LogManager.getLogger(name);

    addAppender(out, name);
    logger.warn("warn message");
    final String actual = out.toString();

    Assertions.assertTrue(actual.contains("warn message"));
  }
}
