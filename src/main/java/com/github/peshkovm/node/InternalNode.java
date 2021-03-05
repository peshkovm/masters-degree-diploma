package com.github.peshkovm.node;

import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.github.peshkovm.common.component.BeanFactoryBuilder;
import com.github.peshkovm.common.component.ComponentConfiguration;
import com.github.peshkovm.common.component.LifecycleService;
import com.github.peshkovm.common.config.ConfigBuilder;
import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/** Default implementation of {@link Node} interface. */
public class InternalNode extends AbstractLifecycleComponent implements Node {
  private final Config config;
  private final BeanFactory beanFactory;

  /**
   * Initializes a newly created {@code InternalNode} object with {@link Config} instance containing
   * fields from application.conf file.
   */
  public InternalNode() {
    this(new ConfigBuilder().build());
  }

  /**
   * Initializes a newly created {@code InternalNode} object with {@link Config config} argument.
   *
   * @param config config to be used by InternalNode instance
   */
  public InternalNode(Config config) {
    this.config = config;
    logger.info("Initializing...");
    final BeanFactoryBuilder beanFactoryBuilder = new BeanFactoryBuilder();
    beanFactoryBuilder.addBean(config, bd -> bd.setScope(ConfigurableBeanFactory.SCOPE_SINGLETON));
    beanFactoryBuilder.add(ComponentConfiguration.class);

    beanFactory = beanFactoryBuilder.createBeanFactory();
    logger.info("Initialized");
  }

  public HostAndPort getHostAndPort() {
    return HostAndPort.fromParts(
        config.getString("transport.host"), config.getInt("transport.port"));
  }

  @Override
  protected void doStart() {
    beanFactory.getBean(LifecycleService.class).start();
  }

  @Override
  protected void doStop() {
    beanFactory.getBean(LifecycleService.class).stop();
  }

  @Override
  protected void doClose() {
    beanFactory.getBean(LifecycleService.class).close();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InternalNode)) {
      return false;
    }

    InternalNode that = (InternalNode) o;

    return getHostAndPort() != null
        ? getHostAndPort().equals(that.getHostAndPort())
        : that.getHostAndPort() == null;
  }

  @Override
  public int hashCode() {
    return getHostAndPort() != null ? getHostAndPort().hashCode() : 0;
  }
}
