package com.iluwatar.urm.domain;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by moe on 10.04.16.
 */
public class DomainField {
  private Field field;

  public DomainField(Field field) {
    this.field = field;
  }

  /**
   * get the name of the field.
   *
   * @return name
   */
  public String getUmlName() {
    if (field.isEnumConstant()) {
      // If this is an enum constant, we dont need the type
      return field.getName();
    }
    return field.getName() + " : " + TypeUtils.getSimpleName(field.getGenericType());
  }

  public Visibility getVisibility() {
    return TypeUtils.getVisibility(field.getModifiers());
  }

  public DomainClass getType() {
    return new DomainClass(field.getType());
  }

  public boolean isStatic() {
    return Modifier.isStatic(field.getModifiers());
  }

  public boolean isAbstract() {
    return Modifier.isAbstract(field.getModifiers());
  }

}
