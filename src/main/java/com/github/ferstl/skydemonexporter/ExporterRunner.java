package com.github.ferstl.skydemonexporter;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Callable;
import com.github.ferstl.skydemonexporter.ExporterRunner.FileVersionProvider;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import static com.github.ferstl.skydemonexporter.EnvironmentUtil.expandUserHome;
import static com.github.ferstl.skydemonexporter.EnvironmentUtil.readAirfieldNames;

@Command(name = "sdexport", versionProvider = FileVersionProvider.class)
public class ExporterRunner implements Callable<Integer> {

  @Option(
      names = {"-h", "--help"},
      usageHelp = true,
      description = "Display this help message"
  )
  private boolean helpRequested;

  @Option(
      names = {"-v", "--version"},
      versionHelp = true,
      description = "Display version information"
  )
  private boolean versionRequested;

  @Option(
      names = {"-d", "--data"},
      paramLabel = "DIRECTORY",
      description = "Directory containing SkyDemon's index.xml and data."
  )
  private String platesDir;

  @Parameters(
      paramLabel = "TARGET",
      description = "Directory to export the data. It will be created if it does not exist.")
  private String targetDir;

  public static void main(String[] args) throws Exception {
    int status = new CommandLine(new ExporterRunner()).execute(args);
    System.exit(status);
  }

  @Override
  public Integer call() throws Exception {
    Path platesDir = (this.platesDir != null) ? Path.of(expandUserHome(this.platesDir)) : EnvironmentUtil.findSkyDemonData();
    Path targetDir = Path.of(expandUserHome(this.targetDir));
    Properties airfieldNames = readAirfieldNames();

    new Exporter(airfieldNames, new DocumentHandler(platesDir, targetDir)).export();
    return 0;
  }

  // Make sure to keep src/main/resources/META-INF in sync!
  static class FileVersionProvider implements IVersionProvider {

    public String[] getVersion() throws Exception {
      URL url = getClass().getResource("/version.txt");
      if (url == null) {
        return new String[]{"${COMMAND-FULL-NAME} unknown version"};
      }

      try (InputStream is = url.openStream()) {
        String version = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return new String[]{"${COMMAND-FULL-NAME} " + version};
      }
    }
  }
}
