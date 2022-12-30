package com.iluwatar.urm.domain;

/**
 * Visibility.
 */
public enum Visibility {
  PUBLIC("+"), PROTECTED("#"), DEFAULT("~"), PRIVATE("-");

  private final String umlRepresentation;

  Visibility(String umlRepresentation) {
    this.umlRepresentation = umlRepresentation;
  }

  public String getUmlRepresentation() {
    return umlRepresentation;
  }

  @Override
  public String toString() {
    return getUmlRepresentation();
  }
}
