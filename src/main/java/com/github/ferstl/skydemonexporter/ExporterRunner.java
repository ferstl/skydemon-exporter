package com.github.ferstl.skydemonexporter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class ExporterRunner implements Callable<Integer> {

  private static final Pattern UUID_PATTERN = Pattern.compile("[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}");

  @Option(
      names = {"-h", "--help"},
      usageHelp = true,
      description = "Display this help message"
  )
  private boolean helpRequested;

  @Option(
      names = {"-d", "--data"},
      paramLabel = "DIRECTORY",
      description = "Directory containing the SkyDemon files and the index.xml"
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
    Path platesDir = this.platesDir != null ? Path.of(expandUserHome(this.platesDir)) : findSkyDemonData();
    Path targetDir = Path.of(expandUserHome(this.targetDir));
    Properties airfieldNames = readAirfieldNames();

    new Exporter(airfieldNames, platesDir, targetDir).export();
    return 0;
  }

  private static Path findSkyDemonData() throws IOException {
    Path targetDir = Paths.get(System.getProperty("user.home"), "Library", "Containers");

    try (Stream<Path> files = Files.list(targetDir)) {
      Path candidateDirectory = files
          .filter(Files::isReadable)
          .filter(Files::isDirectory)
          .filter(directory -> UUID_PATTERN.matcher(directory.getFileName().toString()).matches())
          .filter(Files::isExecutable)
          .filter(directory -> Files.exists(directory.resolve("Data/Library/Plates/index.xml")))
          .findAny()
          .orElseThrow(() -> new IllegalStateException("No candidate SkyDemon directory found in " + targetDir));

      return candidateDirectory.resolve("Data/Library/Plates");
    }
  }

  private static Properties readAirfieldNames() {
    Properties properties = new Properties();
    try {
      properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("airfields.properties"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return properties;
  }

  private static String expandUserHome(String path) {
    return path.replaceFirst("^~", System.getProperty("user.home"));
  }
}
