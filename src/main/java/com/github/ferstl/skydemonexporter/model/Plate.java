package com.github.ferstl.skydemonexporter.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Plate(
    @JacksonXmlProperty(localName = "ID")
    String id,
    int publisher,
    String name,
    String originalFilename,
    String category,
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    LocalDateTime effectiveDate,
    int type,
    boolean withdrawn
) {

}
