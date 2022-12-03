package com.iluwatar.urm.domain;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.DefaultParanamer;
import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * Created by moe on 09.04.16.
 */
public abstract class DomainExecutable<T extends Executable> {

  private static final boolean useParameterNames = Boolean.parseBoolean(
      System.getProperty("useParameterNames", "true"));
  private static final String JAVA_IDENTIFIER_REGEX = "([a-zA-Z_$][a-zA-Z\\d_$]*)";

  private static final Paranamer PARANAMER = new AdaptiveParanamer(new DefaultParanamer(),
      new BytecodeReadingParanamer(), new NullParanamer());
  private T executable;

  public DomainExecutable(T executable) {
    this.executable = executable;
  }

  protected T getExecutable() {
    return executable;
  }

  protected String getName() {
    return executable.getName();
  }

  /**
   * construct the executable name.
   *
   * @return name
   */
  public String getUmlName() {
    // Have to do the old for loop way because Paranamer doesnt have a nice interface
    String[] parameterNames = PARANAMER.lookupParameterNames(executable, false);
    Parameter[] parameters = executable.getParameters();

    Class<?> declaringClass = executable.getDeclaringClass();
    if ((declaringClass.isLocalClass() || declaringClass.isMemberClass())
        && !Modifier.isStatic(declaringClass.getModifiers())
        && (this instanceof DomainConstructor)) {
      // An inner class of any sort (local or member) that isnt static holds a reference to its
      // declaring/parent class. This reference is passed into the constructor
      // as the first argument, so in that case we have to ignore
      // the first argument from 'parameters'
      parameters = Arrays.copyOfRange(parameters, 1, parameters.length);
      // But due to this parameter being hidden, it isnt found by Paranamer
      // and thus the parameterNames array mustn't be cut
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parameters.length; i++) {
      String paraName = ((parameterNames.length != 0 && useParameterNames)
          ? parameterNames[i] + " : " : "");
      sb.append(paraName + TypeUtils.getSimpleName(parameters[i].getParameterizedType()));

      if (i != parameters.length - 1) {
        sb.append(", ");
      }
    }

    return getName() + "(" + sb.toString() + ")";
  }

  public Class<?> getDeclaringClass() {
    return executable.getDeclaringClass();
  }

  public Visibility getVisibility() {
    return TypeUtils.getVisibility(executable.getModifiers());
  }

  public boolean isStatic() {
    return Modifier.isStatic(executable.getModifiers());
  }

  public boolean isAbstract() {
    return Modifier.isAbstract(executable.getModifiers());
  }

  @Override
  public String toString() {
    return getUmlName();
  }
}
