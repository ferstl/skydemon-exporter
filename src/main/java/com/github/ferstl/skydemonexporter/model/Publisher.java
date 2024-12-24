package com.github.ferstl.skydemonexporter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Publisher(
    @JacksonXmlProperty(localName = "ID")
    int id,
    String name) {

}
