package com.github.ferstl.skydemonexporter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import com.github.ferstl.skydemonexporter.model.Plate;

public final class Document implements Comparable<Document> {

  private static final String HEL = "hel";
  private static final Comparator<Document> COMPARATOR = Comparator
      .comparing(Document::icaoCode)
      .thenComparing(Document::type)
      .thenComparing(Document::skydemonDocumentName)
      .thenComparing(Document::effectiveDate)
      .thenComparing(Document::originalDocumentName)
      .thenComparing(Document::name);

  private final String icaoCode;
  private final Type type;
  private String name;
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


  String icaoCode() {
    return this.icaoCode;
  }

  public Type type() {
    return this.type;
  }

  public String originalDocumentName() {
    return this.originalDocumentName;
  }

  public String skydemonDocumentName() {
    return this.skydemonDocumentName;
  }

  public LocalDateTime effectiveDate() {
    return this.effectiveDate;
  }

  public String name() {
    return this.name;
  }

  public String exportFileName() {
    return switch (this.type) {
      case CHART -> this.name + (this.isForHelicopter && !this.nameContainsHelicopter ? " (Hel)" : "") + " " + this.icaoCode;
      case AD_INFO -> "AD INFO " + this.icaoCode + (this.isForHelicopter ? " (Hel)" : "");
      case GUIDE -> this.name;
      case SUPPLEMENT -> (this.nameContainsSupplement ? "" : "SUP ") + this.name.replaceAll("/", "-");
    } + " (" + this.effectiveDate.toLocalDate() + ")" + ".pdf";
  }

  public void deconflictName(int index) {
    this.name = this.name + " " + index;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Document document)) {return false;}
    return Objects.equals(this.icaoCode, document.icaoCode)
        && this.type == document.type
        && Objects.equals(this.skydemonDocumentName, document.skydemonDocumentName)
        && Objects.equals(this.effectiveDate, document.effectiveDate)
        && Objects.equals(this.originalDocumentName, document.originalDocumentName)
        && Objects.equals(this.name, document.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.icaoCode,
        this.type,
        this.originalDocumentName,
        this.effectiveDate,
        this.skydemonDocumentName,
        this.name
    );
  }

  @Override
  public int compareTo(Document o) {
    return COMPARATOR.compare(this, o);
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
