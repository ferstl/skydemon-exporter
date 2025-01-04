package com.github.ferstl.skydemonexporter;

import java.util.Properties;
import com.github.ferstl.skydemonexporter.DocumentHandler.ComparisonResult;

public final class Exporter {

  private final Properties airfieldNames;
  private final DocumentHandler documentHandler;

  public Exporter(Properties airfieldNames, DocumentHandler documentHandler) {
    this.airfieldNames = airfieldNames;
    this.documentHandler = documentHandler;
  }

  public void export() {
    this.documentHandler.initialize();

    ComparisonResult comparisonResult = this.documentHandler.compareData();
    for (Document deletedDocument : comparisonResult.deletedDocuments()) {
      String subDirectory = subDirectoryName(deletedDocument.icaoCode());
      this.documentHandler.delete(subDirectory, deletedDocument);
    }
    for (Document newDocument : comparisonResult.newDocuments()) {
      String subDirectory = subDirectoryName(newDocument.icaoCode());
      this.documentHandler.copy(subDirectory, newDocument);
    }

    this.documentHandler.finish();
  }

  private String subDirectoryName(String icao) {
    if (icao.isBlank()) {
      return ".";
    }

    String airfieldName = this.airfieldNames.getProperty(icao, icao);
    return !airfieldName.equals(icao) ? airfieldName + " (" + icao + ")" : icao;
  }

}
