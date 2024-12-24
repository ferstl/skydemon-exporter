package com.github.ferstl.skydemonexporter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.ferstl.skydemonexporter.model.Airfield;
import com.github.ferstl.skydemonexporter.model.AirfieldPlateIndex;

public class Main {

  private static final int PUBLISHER_SKYGUIDE = 7;
  private static final String NO_AIRFIELD = "no_airfield";
  private static final Pattern UUID_PATTERN = Pattern.compile("[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}");

  public static void main(String[] args) throws Exception {
    Path platesDir = findSkyDemonData();
    Path indexFile = platesDir.resolve("index.xml");
    Path targetDir = Paths.get(System.getProperty("user.home"), "Downloads", "SkyDemon");
    Files.createDirectories(targetDir);

    AirfieldPlateIndex airfieldPlateIndex = readIndex(indexFile);
    Properties airfieldNames = readAirfieldNames();

    Map<String, List<Document>> documentsByAirfield = groupByAirfield(airfieldPlateIndex);

    for (Entry<String, List<Document>> entry : documentsByAirfield.entrySet()) {
      String icao = entry.getKey();
      List<Document> documents = entry.getValue();

      Path airfieldDir = icao.equals(NO_AIRFIELD) ?
          targetDir : targetDir.resolve(airfieldNames.getProperty(icao, icao) + " (" + icao + ")");
      Files.createDirectories(airfieldDir);

      for (Document document : documents) {
        Path sourceFile = platesDir.resolve(document.skydemonDocumentName());
        Path targetFile = airfieldDir.resolve(document.fileName());
        if (Files.exists(sourceFile)) {
          Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } else {
          System.out.println("Skipping " + document.fileName() + " (source file not found)");
        }
      }
    }
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

  private static AirfieldPlateIndex readIndex(Path indexFile) throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE);
    xmlMapper.registerModule(new JavaTimeModule());

    return xmlMapper.readValue(indexFile.toFile(), AirfieldPlateIndex.class);
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

  private static Map<String, List<Document>> groupByAirfield(AirfieldPlateIndex data) {
    Map<String, List<Document>> documentsByAirfield = new HashMap<>();
    for (Airfield airfield : data.airfields()) {
      String icao = Objects.requireNonNullElse(airfield.icao(), NO_AIRFIELD);
      List<Document> documents = airfield.plates().stream()
          .filter(plate -> plate.publisher() == PUBLISHER_SKYGUIDE)
          .map(plate -> Document.fromPlate(icao, plate))
          .collect(Collectors.toList());

      if (!documents.isEmpty()) {
        documentsByAirfield.put(icao, documents);
      }
    }

    return documentsByAirfield;
  }


}
