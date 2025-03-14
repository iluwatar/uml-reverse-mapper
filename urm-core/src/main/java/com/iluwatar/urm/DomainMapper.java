package com.iluwatar.urm;

import com.iluwatar.urm.domain.DomainClass;
import com.iluwatar.urm.domain.Edge;
import com.iluwatar.urm.presenters.Presenter;
import com.iluwatar.urm.presenters.Representation;
import com.iluwatar.urm.scanners.FieldScanner;
import com.iluwatar.urm.scanners.HierarchyScanner;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DomainMapper controls the domain scanning.
 */
public class DomainMapper {

  private static final Logger log = LoggerFactory.getLogger(DomainMapper.class);
  private final FieldScanner fieldScanner;
  private final HierarchyScanner hierarchyScanner;
  private final List<Class<?>> classes;
  private final Presenter presenter;

  public DomainMapper(Presenter presenter, final List<Class<?>> classes) {
    this.presenter = presenter;
    this.classes = classes;
    fieldScanner = new FieldScanner(classes);
    hierarchyScanner = new HierarchyScanner(classes);
  }

  /**
   * method to get representation.
   *
   * @return Representation type
   * @throws ClassNotFoundException exception
   */
  public Representation describeDomain() throws ClassNotFoundException {
    List<Edge> edges = new ArrayList<>();
    edges.addAll(fieldScanner.getEdges());
    edges.addAll(hierarchyScanner.getEdges());
    List<DomainClass> domainObjects = classes.stream().map(DomainClass::new)
        .collect(Collectors.toList());
    return presenter.describe(domainObjects, edges);
  }

  public List<Class<?>> getClasses() {
    return classes;
  }

  public static DomainMapper create(Presenter presenter, List<String> packages,
                                    List<String> ignores, URLClassLoader classLoader)
      throws ClassNotFoundException {
    List<Class<?>> allClasses = DomainClassFinder.findClasses(packages, ignores, classLoader);
    return new DomainMapper(presenter, allClasses);
  }

  public static DomainMapper create(Presenter presenter, final List<String> packages,
                                    List<String> ignores) throws ClassNotFoundException {
    return create(presenter, packages, ignores, null);
  }

  public static DomainMapper create(Presenter presenter, final List<String> packages)
      throws ClassNotFoundException {
    return create(presenter, packages, new ArrayList<>(), null);
  }
}
