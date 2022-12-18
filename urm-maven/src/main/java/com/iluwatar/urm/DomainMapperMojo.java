package com.iluwatar.urm;

import static java.util.stream.Collectors.toList;

import com.iluwatar.urm.presenters.Presenter;
import com.iluwatar.urm.presenters.Representation;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;

/**
 * DomainMapperMojo.
 */
@Mojo(name = "map", defaultPhase = LifecyclePhase.PROCESS_CLASSES,
    requiresDependencyResolution = ResolutionScope.COMPILE)
public class DomainMapperMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project.basedir}", property = "outputDirectory", required = true)
  private File outputDirectory;
  @Component
  private MavenProject project;
  @Parameter(property = "map.packages", required = true)
  private List<String> packages;
  @Parameter(property = "map.ignores", required = false)
  private List<String> ignores;
  @Parameter(property = "presenter", required = false)
  private String presenter;
  @Parameter(property = "map.skipForProjects", required = false)
  private List<String> skipForProjects;
  @Parameter(property = "includeMainDirectory", defaultValue = "true")
  private boolean includeMainDirectory;
  @Parameter(property = "includeTestDirectory", defaultValue = "false")
  private boolean includeTestDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skipForProjects != null && !skipForProjects.isEmpty()) {
      String projectName = project.getName();
      if (skipForProjects.contains(projectName)) {
        getLog().info("Skip configured (in pom.xml) for current project \"" + projectName + "\". "
            + "Plugin will not be executed!");
        return;
      }
    }

    if (packages.isEmpty()) {
      throw new MojoFailureException("No packages defined for scanning.");
    }
    try {
      Presenter selectedPresenter = Presenter.parse(this.presenter);

      String fileName = project.getName() + ".urm." + selectedPresenter.getFileEnding();
      Path path = Paths.get(outputDirectory.getPath(), fileName);
      Path dir = path.getParent();
      if (dir != null) {
        Files.createDirectories(dir);
      }

      if (!Files.exists(path)) {
        List<URL> projectClasspathList = getClasspathUrls();
        DomainMapper mapper = DomainMapper.create(selectedPresenter, packages, ignores,
            new URLClassLoader(projectClasspathList.toArray(new URL[projectClasspathList.size()])));

        Representation representation = mapper.describeDomain();
        Files.write(path, representation.getContent().getBytes());
        getLog().info(fileName + " successfully written to: \""
            + path
            + "\"!");
      } else {
        getLog().info(fileName + " already exists, file was not overwritten!");
      }
    } catch (ClassNotFoundException | DependencyResolutionRequiredException | IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private List<URL> getClasspathUrls() throws DependencyResolutionRequiredException,
      MojoExecutionException {
    List<URL> projectClasspathList = new ArrayList<>();
    for (String element : getProjectClassPathList()) {
      try {
        projectClasspathList.add(new File(element).toURI().toURL());
      } catch (MalformedURLException e) {
        throw new MojoExecutionException(element + " is an invalid classpath element", e);
      }
    }
    return projectClasspathList;
  }

  private List<String> getProjectClassPathList() throws DependencyResolutionRequiredException {
    List<String> projectClasspathList = new ArrayList<>();
    if (includeMainDirectory && includeTestDirectory) {
      projectClasspathList = project.getTestClasspathElements();
    } else if (includeMainDirectory) {
      projectClasspathList = project.getCompileClasspathElements();
    } else if (includeTestDirectory) {
      String outputDir = project.getBuild().getOutputDirectory();
      projectClasspathList = ((List<String>) project.getTestClasspathElements())
          .stream().filter(url -> !outputDir.equalsIgnoreCase(url))
          .collect(toList());
    }
    return projectClasspathList;
  }
}
