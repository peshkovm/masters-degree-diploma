package com.github.peshkovm.node;

import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import com.github.peshkovm.common.component.BeanFactoryBuilder;
import com.github.peshkovm.common.component.ComponentConfiguration;
import com.github.peshkovm.common.component.LifecycleService;
import com.github.peshkovm.common.config.ConfigBuilder;
import com.github.peshkovm.transport.DiscoveryNode;
import com.github.peshkovm.transport.TransportServer;
import com.typesafe.config.Config;
import java.util.Objects;
import lombok.Getter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Default implementation of {@link Node} interface.
 */
public class InternalNode extends AbstractLifecycleComponent implements Node {

  @Getter
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

    final DiscoveryNode thisDiscoveryNode =
        this.getBeanFactory().getBean(TransportServer.class).localNode();
    final DiscoveryNode thatDiscoveryNode =
        that.getBeanFactory().getBean(TransportServer.class).localNode();

    return Objects.equals(thisDiscoveryNode, thatDiscoveryNode);
  }

  @Override
  public int hashCode() {
    final DiscoveryNode thisDiscoveryNode =
        this.getBeanFactory().getBean(TransportServer.class).localNode();

    return thisDiscoveryNode != null ? thisDiscoveryNode.hashCode() : 0;
  }

  @Override
  public BeanFactory getBeanFactory() {
    return beanFactory;
  }
}
