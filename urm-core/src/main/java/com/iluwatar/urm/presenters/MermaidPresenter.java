package com.iluwatar.urm.presenters;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import com.iluwatar.urm.domain.DomainClass;
import com.iluwatar.urm.domain.DomainClassType;
import com.iluwatar.urm.domain.Edge;
import com.iluwatar.urm.domain.EdgeType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MermaidPresenter.
 */
public class MermaidPresenter implements Presenter {
  public static final String FILE_PREAMBLE = "classDiagram";

  private String describePackages(List<DomainClass> domainClasss) {
    return domainClasss.stream()
        .collect(groupingBy(DomainClass::getPackageName))
        .entrySet().stream()
        .map(this::describePackage)
        .collect(joining());
  }

  private String describePackage(Map.Entry<String, List<DomainClass>> entry) {
    return listDomainClasses(entry.getValue());
  }

  private String listDomainClasses(List<DomainClass> domainClasses) {
    return domainClasses.stream()
        .map(this::describeDomainClass)
        .distinct()
        .collect(joining());
  }

  private String describeDomainClass(DomainClass domainClass) {
    return String.format("  %s {\n%s%s%s%s\n  }\n",
        describeDomainClassType(domainClass),
        describeDomainClassTypeAnnotation(domainClass),
        describeDomainClassFields(domainClass),
        describeDomainClassConstructors(domainClass),
        describeDomainClassMethods(domainClass));
  }

  private String describeDomainClassType(DomainClass domainClass) {
    String className = domainClass.getClassName();
    return "class " + className;
  }

  private String describeDomainClassTypeAnnotation(DomainClass domainClass) {
    switch (domainClass.getClassType()) {
      case CLASS:
        return (domainClass.isAbstract() ? "<<abstract>>" : "");
      case INTERFACE:
        return "<<interface>>";
      case ENUM:
        return "<<enumeration>>";
      case ANNOTATION:
        return "<<annotation>>";
      default:
        return "";
    }
  }

  private String describeDomainClassFields(DomainClass domainClass) {
    String description = domainClass.getFields().stream()
        .map(f -> f.getVisibility() + " "
            + f.getUmlName()
           + (f.isStatic() ? "$  " : "")
            + (f.isAbstract() ? "* " : ""))
        .collect(Collectors.joining("\n    "));
    return !description.equals("") ? "\n    " + description : "";
  }

  private String describeDomainClassConstructors(DomainClass domainClass) {
    String description = domainClass.getConstructors().stream()
        .map(c -> c.getVisibility() + " " + c.getUmlName())
        .collect(Collectors.joining("\n    "));
    return !description.equals("") ? "\n    " + description : "";
  }

  private String describeDomainClassMethods(DomainClass domainClass) {
    String description = domainClass.getMethods().stream()
        .map(m -> m.getVisibility() + " "
            + m.getUmlName()
            + (m.isStatic() ? "$ " : "")
            + (m.isAbstract() ? "* " : ""))
        .collect(Collectors.joining("\n    "));
    return !description.equals("") ? "\n    " + description : "";
  }

  @Override
  public Representation describe(List<DomainClass> domainObjects, List<Edge> edges) {
    String content = FILE_PREAMBLE + "\n"
        + describePackages(domainObjects)
        + describeCompositions(edges)
        + describeInheritance(edges);
    return new Representation(content, "mmd");
  }

  private String describeCompositions(List<Edge> edges) {
    return edges.stream()
        .filter(e -> e.type != EdgeType.EXTENDS)
        .map(this::describeComposition)
        .collect(joining());
  }

  private String describeComposition(Edge compositionEdge) {
    return String.format("%s\n", describeEdge(compositionEdge));
  }

  private String describeEdge(Edge edge) {
    String sourceName = edge.source.getClassName();
    String targetName = edge.target.getClassName();

    String arrow = "--";
    // Arrows pointing from Source to Target!
    switch (edge.type) {
      case STATIC_INNER_CLASS:
        arrow = "<..";
        break;
      case INNER_CLASS:
        arrow = "<--";
        break;
      default:
        arrow = "-->";
        break;
    }

    if (edge.source.getDescription() == null) {
      arrow = flip(arrow);
    } else {
      targetName = " \"-" + edge.source.getDescription() + "\" " + targetName;
    }

    return String.format("%s %s %s", sourceName, arrow, targetName);
  }

  private String describeInheritance(List<Edge> edges) {
    return edges.stream()
        .filter(e -> e.type == EdgeType.EXTENDS)
        .map(this::describeInheritance)
        .collect(joining());
  }

  private String describeInheritance(Edge hierarchyEdge) {
    String arrow = "--|>";
    if (hierarchyEdge.target.getClassType() == DomainClassType.INTERFACE
        && hierarchyEdge.source.getClassType() != DomainClassType.INTERFACE) {
      // if target is an interface and source is not, it is officially called
      // realization and uses a dashed line
      arrow = "..|>";
    }

    return String.format("%s %s %s\n",
        hierarchyEdge.source.getClassName(),
        arrow,
        hierarchyEdge.target.getClassName());
  }

  private static String flip(String s) {
    String reversedString = new StringBuilder(s).reverse().toString();
    return reversedString.replaceAll("<", ">").replaceAll(">", "<");
  }

  @Override
  public String getFileEnding() {
    return "mmd";
  }
}
