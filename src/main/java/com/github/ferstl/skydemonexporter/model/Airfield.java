package com.github.ferstl.skydemonexporter.model;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Airfield(
    @JacksonXmlProperty(localName = "ICAO")
    String icao,
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    LocalDateTime validityTime,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Plate")
    List<Plate> plates
) {

  // There might be entries like
  // <Airfield ProductCode="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" ValidityTime="20241228114418" />
  @Override
  public List<Plate> plates() {
    return this.plates != null ? this.plates : List.of();
  }
}
