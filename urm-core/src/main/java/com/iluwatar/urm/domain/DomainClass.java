package com.iluwatar.urm.domain;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by moe on 09.04.16.
 */
public class DomainClass {

  private static final Logger log = LoggerFactory.getLogger(DomainClass.class);
  protected static final List<String> IGNORED_METHODS = Arrays.asList("$jacocoInit");
  protected static final List<String> IGNORED_FIELDS = Arrays.asList("$jacocoData");

  private Class<?> clazz;
  private String description;
  private transient List<DomainField> fieldList;
  private transient List<DomainConstructor> constructorList;
  private transient List<DomainMethod> methodList;

  public DomainClass(Class<?> clazz, String description) {
    this.clazz = clazz;
    this.description = description;
  }

  public DomainClass(Class<?> clazz) {
    this(clazz, null);
  }

  private boolean isLambda(String s) {
    return s.contains("lambda$");
  }

  public String getPackageName() {
    return clazz.getPackage().getName();
  }

  public String getUmlName() {
    return TypeUtils.getSimpleName(clazz);
  }

  public String getClassName() {
    return clazz.getSimpleName();
  }

  public String getDescription() {
    return description;
  }

  /**
   * method to get declared fields of the class.
   *
   * @return list of fields
   */
  public List<DomainField> getFields() {
    if (fieldList == null) {
      fieldList = Arrays.stream(clazz.getDeclaredFields())
          .filter(f -> !(f.getDeclaringClass().isEnum() && f.getName().equals("$VALUES")))
          .filter(f -> !f.isSynthetic())
          .filter(f -> !IGNORED_FIELDS.contains(f.getName()))
          .map(DomainField::new)
          .sorted(Comparator.comparing(DomainField::getUmlName))
          .collect(Collectors.toList());
    }
    return fieldList;
  }

  /**
   * method to get declared constructors of the class.
   *
   * @return list of constructors
   */
  public List<DomainConstructor> getConstructors() {
    if (constructorList == null) {
      if (clazz.isEnum()) {
        // Enums only have the Native Constructor...
        constructorList = Collections.emptyList();
      } else {
        constructorList = Arrays.stream(clazz.getDeclaredConstructors())
            .filter(c -> !c.isSynthetic())
            .map(DomainConstructor::new)
            .sorted(Comparator.comparing(DomainConstructor::getUmlName))
            .collect(Collectors.toList());
      }
    }
    return constructorList;
  }

  /**
   * method to get declared methods of the class.
   *
   * @return list of methods
   */
  public List<DomainMethod> getMethods() {
    if (methodList == null) {
      methodList = Arrays.stream(clazz.getDeclaredMethods())
          .filter(m -> !m.isSynthetic())
          .map(DomainMethod::new)
          .filter(m -> !IGNORED_METHODS.contains(m.getName()) && !isLambda(m.getName()))
          .sorted(Comparator.comparing(DomainExecutable::getUmlName))
          .collect(Collectors.toList());
    }
    return methodList;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public Visibility getVisibility() {
    return TypeUtils.getVisibility(clazz.getModifiers());
  }

  /**
   * method to get class type of the class.
   *
   * @return class type
   */
  public DomainClassType getClassType() {
    if (clazz.isInterface()) {
      return DomainClassType.INTERFACE;
    } else if (clazz.isEnum()) {
      return DomainClassType.ENUM;
    } else if (clazz.isAnnotation()) {
      return DomainClassType.ANNOTATION;
    } else {
      return DomainClassType.CLASS;
    }
  }

  public boolean isAbstract() {
    return Modifier.isAbstract(clazz.getModifiers());
  }

}
