package com.iluwatar.urm;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * Finds domain classes.
 */
public class DomainClassFinder {

  private static final Logger logger = Logger.getLogger(DomainClassFinder.class.getName());

  private static final String URM_PACKAGE = "com.iluwatar.urm";
  public static boolean ALLOW_FINDING_INTERNAL_CLASSES;

  public static ClassLoader[] classLoaders;

  /**
   * method to find and filter classes using reflections.
   *
   * @param packages list of packages
   * @param ignores list of ignores
   * @param classLoader URL classloader object
   * @return list of classes
   */
  public static List<Class<?>> findClasses(final List<String> packages, List<String> ignores,
                                           final ClassLoader classLoader) {
    return packages.stream()
        .map(packageName -> getClasses(classLoader, packageName))
        .flatMap(Collection::stream)
        .filter(DomainClassFinder::isNotPackageInfo)
        .filter(DomainClassFinder::isNotAnonymousClass)
        .filter((Class<?> clazz) -> !ignores.contains(clazz.getName())
            && !ignores.contains(clazz.getSimpleName()))
        .sorted(Comparator.comparing(Class::getName))
        .collect(Collectors.toList());
  }

  private static boolean isNotPackageInfo(Class<?> clazz) {
    return !clazz.getSimpleName().equals("package-info");
  }

  private static boolean isNotAnonymousClass(Class<?> clazz) {
    return !clazz.getSimpleName().equals("");
  }

  private static Set<Class<?>> getClasses(ClassLoader classLoader, String packageName) {
    FilterBuilder filter = new FilterBuilder().includePackage(packageName);
    if (!isAllowFindingInternalClasses()) {
      filter.excludePackage(URM_PACKAGE);
    }
    if (classLoader != null) {
      Reflections reflections = new Reflections(new ConfigurationBuilder()
              .setScanners(new SubTypesScanner(false), new ResourcesScanner())
              .addClassLoaders(classLoader)
              .forPackage(packageName, classLoader)
              .filterInputsBy(filter));
      Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
      classes.addAll(reflections.getSubTypesOf(Enum.class));
      return classes;
    } else {
      Reflections reflections = new Reflections(new ConfigurationBuilder()
              .setScanners(new SubTypesScanner(false), new ResourcesScanner())
              .forPackage(packageName)
              .filterInputsBy(filter));
      Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
      classes.addAll(reflections.getSubTypesOf(Enum.class));
      return classes;
    }
  }

  public static boolean isAllowFindingInternalClasses() {
    return ALLOW_FINDING_INTERNAL_CLASSES |= Boolean.parseBoolean(
        System.getProperty("DomainClassFinder.allowFindingInternalClasses", "false"));
  }

  private DomainClassFinder() {
    // private constructor for utility class
  }
}
