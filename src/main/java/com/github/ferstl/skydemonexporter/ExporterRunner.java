package com.github.ferstl.skydemonexporter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ExporterRunner {

  public static final Pattern UUID_PATTERN = Pattern.compile("[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}");

  public static void main(String[] args) throws Exception {
    Path platesDir = findSkyDemonData();
    Path targetDir = Paths.get(System.getProperty("user.home"), "Downloads", "SkyDemon");

    Properties airfieldNames = readAirfieldNames();

    new Exporter(airfieldNames, platesDir, targetDir).export();
  }

  public static Path findSkyDemonData() throws IOException {
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

  public static Properties readAirfieldNames() {
    Properties properties = new Properties();
    try {
      properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("airfields.properties"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return properties;
  }
}
