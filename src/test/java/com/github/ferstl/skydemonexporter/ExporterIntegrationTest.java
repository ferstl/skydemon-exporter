package com.github.ferstl.skydemonexporter;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static com.github.ferstl.skydemonexporter.EnvironmentUtil.readAirfieldNames;

class ExporterIntegrationTest {

  @Test
  void integrationTest(@TempDir Path tempDir) throws Exception {
    Exporter exporter = new Exporter(readAirfieldNames(), new DocumentHandler(Paths.get("etc/testdata"), tempDir));
    exporter.export();
  }
}
