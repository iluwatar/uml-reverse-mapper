package com.iluwatar.urm.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Edge is a relation between classes.
 */
public class Edge {

  public final DomainClass source;
  public final DomainClass target;
  public final EdgeType type;
  public final Direction direction;

  /**
   * constructor for the class.
   *
   * @param source DomainClass.
   * @param target DomainClass.
   * @param type EdgeType.
   * @param direction Direction.
   */
  public Edge(DomainClass source, DomainClass target, EdgeType type, Direction direction) {
    this.source = source;
    this.target = target;
    this.type = type;
    this.direction = direction;
  }

  public Edge(DomainClass source, DomainClass target, EdgeType type) {
    this(source, target, type, null);
  }

  @Override
  public final int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public final boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
