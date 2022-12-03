package com.iluwatar.urm;

import com.iluwatar.urm.presenters.Presenter;
import com.iluwatar.urm.presenters.Representation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain mapper command line.
 */
public class DomainMapperCli {

  private static final Logger log = LoggerFactory.getLogger(DomainMapperCli.class);
  DomainMapper domainMapper;

  public static void main(final String[] args) throws ClassNotFoundException, IOException {
    new DomainMapperCli().run(args);
  }

  /**
   * run method for cli class.
   *
   * @param args input arguments
   * @throws ClassNotFoundException exception
   * @throws IOException exception
   */
  public void run(final String[] args) throws ClassNotFoundException, IOException {
    // create the command line parser
    CommandLineParser parser = new BasicParser();
    // create the Options
    Options options = new Options();
    options.addOption("f", "file", true, "write to file");
    options.addOption("p", "package", true, "comma separated list of domain packages");
    options.addOption(OptionBuilder.withArgName("package").hasArgs().isRequired().create('p'));
    options.addOption("i", "ignore", true, "comma separated list of ignored types");
    options.addOption(OptionBuilder.withArgName("ignore").hasArgs().isRequired(false).create('i'));
    options.addOption("s", "presenter", true, "presenter to be used");
    options.addOption(OptionBuilder.withArgName("presenter")
        .hasArgs().isRequired(false).create('s'));
    try {
      CommandLine line = parser.parse(options, args);
      String[] packages = line.getOptionValue("p").split(",[ ]*");
      log.debug("Scanning domain for packages:");
      for (String packageName : packages) {
        log.debug(packageName);
      }
      String[] ignores = null;
      if (line.hasOption("i")) {
        ignores = line.getOptionValue("i").split(",[ ]*");
        if (ignores != null) {
          log.debug("Ignored types:");
          for (String ignore : ignores) {
            log.debug(ignore);
          }
        }
      }

      Presenter presenter = Presenter.parse(line.getOptionValue("s"));
      domainMapper = DomainMapper.create(presenter, Arrays.asList(packages),
          ignores == null ? new ArrayList<>() : Arrays.asList(ignores));
      Representation representation = domainMapper.describeDomain();
      if (line.hasOption('f')) {
        String filename = line.getOptionValue('f');
        Path parent = Paths.get(filename).getParent();
        if (parent != null) {
          Files.createDirectories(Paths.get(filename).getParent());
        }
        Files.write(Paths.get(filename), representation.getContent().getBytes());
        log.info("Wrote to file " + filename);
      } else {
        log.info(representation.getContent());
      }
    } catch (ParseException exp) {
      log.info(exp.getMessage());
      // automatically generate the help statement
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar urm-core.jar", options);
    }
  }
}
