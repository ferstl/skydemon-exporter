package com.github.ferstl.skydemonexporter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.ferstl.skydemonexporter.model.Airfield;
import com.github.ferstl.skydemonexporter.model.AirfieldPlateIndex;
import com.github.ferstl.skydemonexporter.model.Plate;

public class DocumentHandler {

  private static final int PUBLISHER_SKYGUIDE = 7;
  private static final Pattern INDEX_FILE_PATTERN = Pattern.compile("index-[0-9]{4}-[0-9]{2}-[0-9]{2}.xml");
  private static final String INDEX_FILE = "index.xml";

  private final Path platesDir;
  private final Path targetDir;
  private final Path metadataDir;

  public DocumentHandler(Path platesDir, Path targetDir) {
    this.platesDir = platesDir;
    this.targetDir = targetDir;
    this.metadataDir = this.targetDir.resolve(".sdexport");
  }

  public void initialize() {
    createDirectory(this.metadataDir);
  }

  public ComparisonResult compareData() {
    Set<Document> newDocuments = createDocuments(readIndex(platesIndexFile()));
    Set<Document> existingDocuments = metadataIndexFile()
        .map(existingIndex -> createDocuments(readIndex(existingIndex)))
        .orElse(new HashSet<>());

    existingDocuments.removeAll(newDocuments);

    return new ComparisonResult(newDocuments, existingDocuments);
  }

  public void delete(String subDirectory, Document deletedDocument) {
    Path subDirectoryPath = this.targetDir.resolve(subDirectory);
    Path documentPath = subDirectoryPath.resolve(deletedDocument.exportFileName());
    try {
      boolean deleted = Files.deleteIfExists(documentPath);
      if (deleted) {
        System.out.println("Deleted " + documentPath.getFileName());
      }

      if (Files.exists(subDirectoryPath)) {
        try (Stream<Path> entries = Files.list(subDirectoryPath)) {
          if (entries.findAny().isEmpty()) {
            System.out.println("Deleting " + subDirectoryPath.getFileName());
            Files.delete(subDirectoryPath);
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }


  }

  public void copy(String subDirectory, Document newDocument) {
    Path sourceFile = this.platesDir.resolve(newDocument.skydemonDocumentName());
    Path targetDirectory = this.targetDir.resolve(subDirectory);
    Path targetFile = targetDirectory.resolve(newDocument.exportFileName());
    createDirectory(targetDirectory);

    if (Files.exists(sourceFile)) {
      if (!Files.exists(targetFile)) {
        copyFile(sourceFile, targetFile);
      }
    } else {
      System.out.println("Skipping " + targetFile + ". File not available in Skydemon.");
    }
  }

  private static void createDirectory(Path targetDirectory) {
    if (!Files.exists(targetDirectory)) {
      try {
        Files.createDirectories(targetDirectory);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  private static void copyFile(Path sourceFile, Path targetFile) {
    try {
      Files.copy(sourceFile, targetFile);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void finish() {
    Optional<Path> oldIndexFile = metadataIndexFile();

    try {
      if (oldIndexFile.isPresent()) {
        Files.delete(oldIndexFile.get());
      }
      Files.copy(this.platesDir.resolve(INDEX_FILE), this.metadataDir.resolve("index-" + LocalDate.now() + ".xml"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private AirfieldPlateIndex readIndex(Path indexFile) {
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE);
    xmlMapper.registerModule(new JavaTimeModule());

    try {
      return xmlMapper.readValue(indexFile.toFile(), AirfieldPlateIndex.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Set<Document> createDocuments(AirfieldPlateIndex data) {
    Set<Document> documents = new TreeSet<>();
    Map<String, Integer> conflictCounters = new HashMap<>();

    for (Airfield airfield : data.airfields()) {
      String icao = Objects.requireNonNullElse(airfield.icao(), "");

      for (Plate plate : airfield.plates()) {
        if (plate.publisher() == PUBLISHER_SKYGUIDE) {
          Document document = Document.fromPlate(icao, plate);
          String conflictKey = document.icaoCode() + document.name();

          int i = conflictCounters.computeIfAbsent(conflictKey, key -> 0) + 1;
          conflictCounters.put(conflictKey, i);
          if (i > 1) {
            document.deconflictName(i);
          }

          documents.add(document);
        }
      }
    }

    return documents;
  }

  private Optional<Path> metadataIndexFile() {
    if (Files.exists(this.metadataDir)) {
      try (Stream<Path> files = Files.list(this.metadataDir)) {
        return files.filter(Files::isRegularFile)
            .filter(file -> INDEX_FILE_PATTERN.matcher(file.getFileName().toString()).matches())
            .max(Comparator.naturalOrder());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    return Optional.empty();
  }

  private Path platesIndexFile() {
    return this.platesDir.resolve(INDEX_FILE);
  }

  public record ComparisonResult(Set<Document> newDocuments, Set<Document> deletedDocuments) {

  }
}
