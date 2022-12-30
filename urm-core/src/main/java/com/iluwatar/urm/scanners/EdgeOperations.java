package com.iluwatar.urm.scanners;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.iluwatar.urm.domain.Direction;
import com.iluwatar.urm.domain.DomainClass;
import com.iluwatar.urm.domain.Edge;
import com.iluwatar.urm.domain.EdgeType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Operations on edges.
 */
public class EdgeOperations {

  /**
   * method for creating new edges.
   *
   * @param sourceClass type of class
   * @param field type of class
   * @param type type of EdgeType
   * @param name type of string
   * @return edge
   */
  public static Edge createEdge(Class<?> sourceClass, Class<?> field, EdgeType type, String name) {
    DomainClass source = new DomainClass(sourceClass, name);
    DomainClass target = new DomainClass(field);
    return new Edge(source, target, type, Direction.UNI_DIRECTIONAL);
  }

  /**
   * create a list of unidirectional and bidirectional.
   * edges
   *
   * @param edges type of list
   * @return list of edges
   */
  public static List<Edge> mergeBiDirectionals(List<Edge> edges) {
    HashSet<Edge> noDuplicateSet = new HashSet<>(edges);
    Collection<List<Edge>> groupedEdges = groupEdges(noDuplicateSet);
    List<Edge> uniDirectionals = takeSingleItemsGroups(groupedEdges);
    List<Edge> biDirectionals = mergeNonSingleGroups(groupedEdges);
    List<Edge> mergedEdges = new ArrayList<>();
    mergedEdges.addAll(uniDirectionals);
    mergedEdges.addAll(biDirectionals);
    return mergedEdges;
  }

  private static List<Edge> takeSingleItemsGroups(Collection<List<Edge>> groupedEdges) {
    return groupedEdges.stream()
        .filter(edgeGroup -> edgeGroup.size() == 1 || 1 == edgeGroup.stream()
            .filter(e -> e.type.isCardinality())
            .count())
        .flatMap(Collection::stream)
        .collect(toList());
  }

  private static List<Edge> mergeNonSingleGroups(Collection<List<Edge>> groupedEdges) {
    List<List<List<Edge>>> edgeGroups = groupedEdges.stream()
        .filter(edgeGroup -> edgeGroup.size() > 1 && 1 < edgeGroup.stream()
            .filter(e -> e.type.isCardinality())
            .count())
        .map(EdgeOperations::groupBySource)
        .collect(toList());
    List<Edge> multiReferenceUniDirectionals = edgeGroups.stream()
        .filter(sourceGroups -> sourceGroups.size() == 1)
        .flatMap(Collection::stream)
        .flatMap(Collection::stream)
        .collect(toList());
    List<Edge> biDirectionals = edgeGroups.stream()
        .filter(sourceGroups -> sourceGroups.size() == 2)
        .map(EdgeOperations.Tuple::createPairs)
        .flatMap(Collection::stream)
        .map(EdgeOperations::mergeEdges)
        .collect(toList());
    List<Edge> newEdges = new ArrayList<>();
    newEdges.addAll(multiReferenceUniDirectionals);
    newEdges.addAll(biDirectionals);
    return newEdges;
  }

  private static Collection<List<Edge>> groupEdges(Set<Edge> edges) {
    return edges.stream()
        .collect(groupingBy(EdgeOperations::sameSourceAndTarget))
        .values();
  }

  private static List<List<Edge>> groupBySource(List<Edge> edges) {
    return new ArrayList<>(edges.stream()
        .collect(groupingBy(edge -> edge.source.getClassName()))
        .values());
  }

  private static Edge mergeEdges(Tuple<Edge, Edge> edgePair) {
    Edge source = edgePair.left;
    Edge target = edgePair.right;
    return new Edge(source.source, target.source,
        EdgeType.resolveEdgeType(source.type, target.type), Direction.BI_DIRECTIONAL);
  }

  private static UnorderedTuple<?, ?> sameSourceAndTarget(Edge edge) {
    String sourceId = edge.source.getPackageName() + "." + edge.source.getClassName();
    String targetId = edge.target.getPackageName() + "." + edge.target.getClassName();
    return UnorderedTuple.of(sourceId, targetId);
  }

  /**
   * method to get matching edge.
   *
   * @param fieldEdges type of list
   * @param e type of Edge
   * @return boolean
   */
  public static boolean relationAlreadyExists(List<Edge> fieldEdges, Edge e) {
    return fieldEdges.stream().anyMatch((Edge d) -> EdgeOperations.isSameRelation(d, e));
  }

  /**
   * method to get matching relation.
   *
   * @param fieldEdges type of list
   * @param e type of Edge
   * @return optional edge
   */
  public static Optional<Edge> getMatchingRelation(List<Edge> fieldEdges, Edge e) {
    List<Edge> edges = fieldEdges.stream()
        .filter((Edge d) -> EdgeOperations.isSameRelation(d, e))
        .collect(toList());
    if (edges.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(edges.get(0));
    }
  }

  private static boolean isSameRelation(Edge d, Edge e) {
    return d.source.getPackageName().equals(e.source.getPackageName())
        && d.source.getClassName().equals(e.source.getClassName())
        && d.target.getPackageName().equals(e.target.getPackageName())
        && d.target.getClassName().equals(e.target.getClassName())
        && d.type.equals(e.type)
        && d.direction.equals(e.direction);
  }

  private static class UnorderedTuple<X, Y> extends Tuple<X, Y> {

    public UnorderedTuple(X left, Y right) {
      super(left, right);
    }

    @Override
    public int hashCode() {
      return left.hashCode() + right.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      } else if (obj instanceof UnorderedTuple) {
        UnorderedTuple<?, ?> tuple = (UnorderedTuple) obj;
        return this.left.equals(tuple.left) && this.right.equals(tuple.right)
            || this.left.equals(tuple.right) && this.right.equals(tuple.left);
      } else {
        return false;
      }
    }

    public static <X, Y> UnorderedTuple<X, Y> of(X source, Y target) {
      return new UnorderedTuple<>(source, target);
    }
  }

  private static class Tuple<X, Y> {
    protected final X left;
    protected final Y right;

    public Tuple(X left, Y right) {
      this.left = left;
      this.right = right;
    }

    public static <T> List<Tuple<T, T>> createPairs(List<List<T>> listOfTwoGroups) {
      List<T> a = listOfTwoGroups.get(0);
      List<T> b = listOfTwoGroups.get(1);
      return makePairs(a, b);
    }

    private static <T> List<Tuple<T, T>> makePairs(List<T> a, List<T> b) {
      List<Tuple<T, T>> pairs = new ArrayList<>();
      if (a.size() > b.size()) {
        for (int i = 0; i < a.size(); i++) {
          pairs.add(new Tuple<>(a.get(i), b.get(i % b.size())));
        }
      } else {
        for (int i = 0; i < b.size(); i++) {
          pairs.add(new Tuple<>(a.get(i % a.size()), b.get(i)));
        }
      }
      return pairs;
    }
  }
}
