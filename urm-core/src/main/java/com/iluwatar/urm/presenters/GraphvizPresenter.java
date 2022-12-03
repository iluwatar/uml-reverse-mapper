package com.iluwatar.urm.presenters;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import com.iluwatar.urm.domain.DomainClass;
import com.iluwatar.urm.domain.Edge;
import com.iluwatar.urm.domain.EdgeType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GraphVizPresenter.
 */
public class GraphvizPresenter implements Presenter {

  public static final String DOMAIN_DECLARATION = "digraph domain {\n";
  public static final String DEFAULTS = "  edge [ fontsize = 11 ];\n  node [ shape=record ];";
  private static final String INHERITANCE_STYLE = "arrowhead=empty color=slategray";
  private final AtomicInteger count = new AtomicInteger();

  private Object getEdgeDescription(Edge edge) {
    StringBuilder sb = new StringBuilder();
    sb.append(" ").append(linkDirection(edge));
    return sb.toString();
  }

  private String linkDirection(Edge edge) {
    if (edge.source.getDescription() == null) {
      return "dir=forward arrowhead=odiamond color=slategray";
    }
    if (edge.target.getDescription() == null) {
      return "dir=back arrowtail=odiamond color=slategray";
    }
    return "dir=both arrowhead=none arrowtail=none color=slategray";
  }

  private String describeInheritance(List<Edge> edges) {
    return edges.stream()
        .filter(e -> e.type == EdgeType.EXTENDS)
        .map(this::describeInheritance)
        .collect(joining());
  }

  private String describeInheritance(Edge hierarchyEdge) {
    return String.format("  %s -> %s [%s];\n",
        hierarchyEdge.source.getClassName(),
        hierarchyEdge.target.getClassName(),
        INHERITANCE_STYLE);
  }

  private String describePackages(List<DomainClass> domainObjects) {
    count.set(0);
    return domainObjects.stream()
        .collect(groupingBy(DomainClass::getPackageName))
        .entrySet().stream()
        .map(this::describePackage)
        .collect(joining());
  }

  private String describePackage(Map.Entry<String, List<DomainClass>> entry) {
    return String.format("  subgraph cluster_%s {\n    label = \"%s\";\n%s  }\n",
        count.getAndIncrement(),
        entry.getKey(),
        listDomainObjects(entry.getValue()));
  }

  private String listDomainObjects(List<DomainClass> domainObjects) {
    return domainObjects.stream()
        .map(domainObject -> describeDomainObject(domainObject))
        .distinct()
        .collect(joining());
  }

  private String describeDomainObject(DomainClass domainObject) {
    return String.format("    %s [ label = \"{%s | %s}\" ] \n",
        domainObject.getClassName(), domainObject.getClassName(),
        describeDomainObjectMethods(domainObject));
  }

  private String describeDomainObjectMethods(DomainClass domainObject) {
    StringBuilder sb = new StringBuilder();
    domainObject.getMethods().stream().forEach((m) -> sb.append("+ " + m + "\\l"));
    return sb.toString();
  }

  private String describeCompositions(List<Edge> edges) {
    return edges.stream()
        .filter(e -> e.type != EdgeType.EXTENDS)
        .map(this::describeComposition)
        .collect(joining());
  }

  private String describeComposition(Edge compositionEdge) {
    return String.format("  %s\n", describeEdge(compositionEdge));
  }

  private String describeEdge(Edge edge) {
    return String.format("%s -> %s [%s];", edge.source.getClassName(),
        edge.target.getClassName(), getEdgeDescription(edge));
  }

  @Override
  public Representation describe(List<DomainClass> domainObjects, List<Edge> edges) {
    String content = DOMAIN_DECLARATION + DEFAULTS + "\n"
        + describePackages(domainObjects)
        + describeCompositions(edges)
        + describeInheritance(edges)
        + "}";
    return new Representation(content, "dot");
  }

  @Override
  public String getFileEnding() {
    return "dot";
  }
}
