package com.iluwatar.urm;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class DomainMapperMojoTest {
  public static final String PROJECT_NAME = "domain-mapper-mojo-test";
  @Spy
  File outputDirectory = new File("");
  @Mock
  MavenProject project;
  @Spy
  List<String> packages = new ArrayList<>();
  @Spy
  List<String> ignores = new ArrayList<>();
  @InjectMocks
  DomainMapperMojo mojo = new DomainMapperMojo();
  final List<Class<?>> dummyList = Arrays.<Class<?>>asList(String.class, Integer.class);

  /**
   * setup method for the test.
   * @throws ClassNotFoundException exception
   * @throws DependencyResolutionRequiredException exception
   */
  @SuppressWarnings("unchecked")
  @Before
  public void setup() throws ClassNotFoundException, DependencyResolutionRequiredException {
    MockitoAnnotations.initMocks(this);
    when(project.getCompileClasspathElements()).thenReturn(Arrays.asList("foo", "bar"));
    when(project.getName()).thenReturn(PROJECT_NAME);
    DomainClassFinder.ALLOW_FINDING_INTERNAL_CLASSES = true;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void testPlantUmlExecute() throws MojoExecutionException,
          MojoFailureException, IOException {
    packages.add("com.iluwatar.urm.testdomain");
    Path pumlPath = Paths.get(PROJECT_NAME + ".urm.puml");
    try {
      mojo.execute();
      assertTrue(Files.exists(pumlPath));
      List<String> strings = Files.readAllLines(pumlPath);
      assertEquals(1, strings.stream().filter(s -> s.contains("package com.iluwatar.urm.testdomain")).count());
      assertEquals(2, strings.stream().filter(s -> s.contains("TestPojo")).count());
    } finally {
      Files.delete(pumlPath);
    }
  }

  @Test(expected = MojoFailureException.class)
  public void failsWithNoPackagesDefined() throws MojoExecutionException,
          MojoFailureException {
    mojo.execute();
  }
}
