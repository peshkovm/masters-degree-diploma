package com.github.peshkovm.main.common;

import com.github.peshkovm.diagram.DiagramFactorySingleton;
import com.github.peshkovm.diagram.commons.DrawIOColor;
import com.github.peshkovm.node.InternalNode;
import java.util.ArrayList;
import java.util.List;

public abstract class TestUtilsWithDiagram extends TestUtils {

  @Override
  protected void createAndStartInternalNode(Object... optionalBeans) {
    final List<Object> optionalBeansList = new ArrayList<>(List.of(optionalBeans));
    optionalBeansList.add(getDiagramInstance());

    super.createAndStartInternalNode(optionalBeansList.toArray());
  }

  @Override
  protected void tearDownNodes() {
    buildDiagram();
    super.tearDownNodes();
  }

  private void buildDiagram() {
    final DiagramFactorySingleton diagramFactorySingleton =
        nodes.map(node -> node.getBeanFactory().getBean(DiagramFactorySingleton.class)).get(0);

    diagramFactorySingleton.buildDiagram();
  }

  protected void addNodesToDiagram() {
    final DiagramFactorySingleton diagramFactorySingleton =
        nodes.map(node -> node.getBeanFactory().getBean(DiagramFactorySingleton.class)).get(0);

    for (int i = 0; i < nodes.size(); i++) {
      final InternalNode internalNode = nodes.get(i);
      diagramFactorySingleton.addNode(internalNode, DrawIOColor.values()[i]);
    }
  }

  protected abstract DiagramFactorySingleton getDiagramInstance();
}
