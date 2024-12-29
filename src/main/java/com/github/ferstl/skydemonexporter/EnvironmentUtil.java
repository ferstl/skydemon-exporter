package com.github.ferstl.skydemonexporter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class EnvironmentUtil {

  private static final Pattern UUID_PATTERN = Pattern.compile("[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}");

  static Path findSkyDemonData() throws IOException {
    String os = System.getProperty("os.name");

    if (os.toLowerCase().startsWith("mac")) {
      return findSkyDemonDataForMac();
    }
    if (os.toLowerCase().startsWith("windows")) {
      return findSkyDemonDataForWindows();
    }

    throw new IllegalStateException("Unsupported OS: " + os);
  }

  private static Path findSkyDemonDataForMac() throws IOException {
    Path dataDir = Paths.get(System.getProperty("user.home"), "Library", "Containers");

    try (Stream<Path> files = Files.list(dataDir)) {
      Path candidateDirectory = files
          .filter(Files::isReadable)
          .filter(Files::isDirectory)
          .filter(directory -> UUID_PATTERN.matcher(directory.getFileName().toString()).matches())
          .filter(Files::isExecutable)
          .filter(directory -> Files.exists(directory.resolve("Data/Library/Plates/index.xml")))
          .findAny()
          .orElseThrow(() -> new IllegalStateException("No candidate SkyDemon directory found in " + dataDir));

      return candidateDirectory.resolve("Data/Library/Plates");
    }
  }

  private static Path findSkyDemonDataForWindows() throws IOException {
    Path indexLocation = Paths.get(System.getenv("APPDATA"), "Divelements Limited", "SkyDemon Plan", "Plates", "index.xml");

    if (!Files.exists(indexLocation)) {
      return indexLocation.getParent();
    }

    throw new IllegalStateException("No SkyDemon data found in " + indexLocation.getParent());
  }

  static Properties readAirfieldNames() {
    Properties properties = new Properties();
    try {
      properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("airfields.properties"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return properties;
  }

  static String expandUserHome(String path) {
    return path.replaceFirst("^~", System.getProperty("user.home"));
  }
}
