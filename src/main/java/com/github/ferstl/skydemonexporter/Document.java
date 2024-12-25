package com.github.ferstl.skydemonexporter;

import java.time.LocalDateTime;
import java.util.Optional;
import com.github.ferstl.skydemonexporter.model.Plate;

public class Document {

  private static final String HEL = "hel";

  private final String icaoCode;
  private final Type type;
  private final String name;
  private final LocalDateTime effectiveDate;
  private final boolean isForHelicopter;
  private final boolean nameContainsHelicopter;
  private final boolean nameContainsSupplement;
  // Name of the document in the national AIP
  private final String originalDocumentName;
  // Name of the document in Skydemon (e.g. 123456.pdf)
  private final String skydemonDocumentName;

  public static Document fromPlate(String icaoCode, Plate plate) {
    return new Document(
        icaoCode,
        Type.fromPlate(plate),
        plate.name(),
        plate.effectiveDate(),
        plate.originalFilename().toLowerCase().contains(HEL),
        plate.originalFilename(),
        plate.id() + ".pdf");
  }

  private Document(String icaoCode, Type type, String name, LocalDateTime effectiveDate, boolean isForHelicopter, String originalDocumentName, String skydemonDocumentName) {
    this.type = type;
    this.name = name;
    this.effectiveDate = effectiveDate;
    this.isForHelicopter = isForHelicopter;
    this.icaoCode = icaoCode;
    this.originalDocumentName = originalDocumentName;
    this.skydemonDocumentName = skydemonDocumentName;

    this.nameContainsHelicopter = name.toLowerCase().contains(HEL);
    this.nameContainsSupplement = name.startsWith("SUP");
  }


  Optional<String> icaoCode() {
    return Optional.ofNullable(this.icaoCode);
  }

  public String originalDocumentName() {
    return this.originalDocumentName;
  }

  public String skydemonDocumentName() {
    return this.skydemonDocumentName;
  }

  public String exportFileName() {
    return exportName() + ".pdf";
  }

  public String exportName() {
    return exportNameWithoutDate() + " (" + this.effectiveDate.toLocalDate() + ")";
  }

  public String exportNameWithoutDate() {
    return switch (this.type) {
      case CHART -> this.name + (this.isForHelicopter && !this.nameContainsHelicopter ? " (Hel)" : "") + " " + this.icaoCode;
      case AD_INFO -> "AD INFO " + this.icaoCode + (this.isForHelicopter ? " (Hel)" : "");
      case GUIDE -> this.name;
      case SUPPLEMENT -> (this.nameContainsSupplement ? "" : "SUP ") + this.name.replaceAll("/", "-");
    };
  }

  public enum Type {
    AD_INFO,
    CHART,
    SUPPLEMENT,
    GUIDE;

    public static Type fromPlate(Plate plate) {
      return switch (plate.type()) {
        case 4 -> CHART;
        case 8 -> plate.name().startsWith("VFR Guide") ? GUIDE : SUPPLEMENT;
        default -> AD_INFO;
      };
    }
  }

}
