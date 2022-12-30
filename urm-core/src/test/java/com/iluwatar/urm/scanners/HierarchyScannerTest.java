package com.iluwatar.urm.scanners;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.iluwatar.urm.domain.DomainClass;
import com.iluwatar.urm.domain.Edge;
import com.iluwatar.urm.domain.EdgeType;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class HierarchyScannerTest {

  private Edge parentToChild = new Edge(new DomainClass(Child.class),
      new DomainClass(Parent.class), EdgeType.EXTENDS);
  private Edge childToGrandChild = new Edge(new DomainClass(GrandChild.class),
      new DomainClass(Child.class), EdgeType.EXTENDS);
  private Edge carToVehicle = new Edge(new DomainClass(Car.class),
      new DomainClass(Vehicle.class), EdgeType.EXTENDS);
  private Edge carToTransport = new Edge(new DomainClass(Car.class),
      new DomainClass(Transport.class), EdgeType.EXTENDS);

  @Test
  public void createsProperHierarchy() {
    List<Class<?>> hierarchicalClasses = new ArrayList<>();
    hierarchicalClasses.add(Child.class);
    hierarchicalClasses.add(GrandChild.class);
    hierarchicalClasses.add(Parent.class);
    HierarchyScanner scanner = new HierarchyScanner(hierarchicalClasses);

    List<Edge> edges = scanner.getEdges();
    assertThat(edges, containsInAnyOrder(parentToChild, childToGrandChild));
  }

  @Test
  public void doesNotIncludeNonDomainParents() {
    List<Class<?>> hierarchicalClasses = new ArrayList<>();
    hierarchicalClasses.add(Child.class);
    hierarchicalClasses.add(GrandChild.class);
    HierarchyScanner scanner = new HierarchyScanner(hierarchicalClasses);

    List<Edge> edges = scanner.getEdges();
    assertThat(edges, contains(childToGrandChild));
  }

  @Test
  public void createsProperHierarchyForInterfaces() {
    List<Class<?>> hierarchicalClasses = new ArrayList<>();
    hierarchicalClasses.add(Car.class);
    hierarchicalClasses.add(Vehicle.class);
    hierarchicalClasses.add(Transport.class);
    HierarchyScanner scanner = new HierarchyScanner(hierarchicalClasses);

    List<Edge> edges = scanner.getEdges();
    assertThat(edges, containsInAnyOrder(carToVehicle, carToTransport));
  }

  private static class Parent {

  }

  private static class Child extends Parent {

  }

  private static class GrandChild extends Child {

  }

  private interface Vehicle {

  }

  private interface Transport {

  }

  private static class Car implements Vehicle, Transport {

  }
}
