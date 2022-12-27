package com.iluwatar.urm;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.qualitytest.CoverageForPrivateConstructor;
import org.junit.Before;
import org.junit.Test;


public class DomainClassFinderTest {

  @Before
  public void setup() {
    DomainClassFinder.ALLOW_FINDING_INTERNAL_CLASSES = true;
  }

  @Test
  public void withSinglePackage() throws Exception {
    List<Class<?>> classes = findClasses(ClassLoader.getSystemClassLoader(),"com.iluwatar.urm.testdomain.person");
    assertThat(classes.size(), is(4));
  }

  @Test
  public void withSinglePackageAndIgnore() throws Exception {
    List<Class<?>> classes = DomainClassFinder.findClasses(
        Arrays.asList("com.iluwatar.urm.testdomain.person"),
        Arrays.asList("Manager"), ClassLoader.getSystemClassLoader());
    assertThat(classes.size(), is(3));
  }

  @Test
  public void withMultiPackages() throws Exception {
    List<Class<?>> classes = findClasses(ClassLoader.getSystemClassLoader(), "com.iluwatar.urm.testdomain.person",
        "com.iluwatar.urm.testdomain.another");
    assertThat(classes.size(), is(5));
  }

  @Test
  public void withMultiPackagesAndIgnores() throws Exception {
    List<Class<?>> classes = DomainClassFinder.findClasses(
        Arrays.asList("com.iluwatar.urm.testdomain.person", "Another"),
        Arrays.asList("Manager", "Another"), ClassLoader.getSystemClassLoader());
    assertThat(classes.size(), is(3));
  }

  @Test
  public void filtersAnonymousClasses() throws Exception {
    List<Class<?>> classes = findClasses(ClassLoader.getSystemClassLoader(), "com.iluwatar.urm.testdomain.withanonymousclass");
    assertThat(classes.size(), is(1));
  }

  @Test
  public void privateConstructor() {
    CoverageForPrivateConstructor.giveMeCoverage(DomainClassFinder.class);
  }

  @Test
  public void findExternalClasses() {
    URLClassLoader loader = URLClassLoader.newInstance(new URL[] { ClassLoader.getSystemClassLoader().getResource("abstract-document.jar")});
    List<Class<?>> classes = findClasses(loader, "com.iluwatar.abstractdocument");
    assertThat(classes.size(), is(10));
  }

  private List<Class<?>> findClasses(ClassLoader classLoader, String... packages) {
    return DomainClassFinder.findClasses(Arrays.asList(packages), new ArrayList<>(), classLoader);
  }
}
