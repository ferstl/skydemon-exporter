package com.github.ferstl.skydemonexporter;

import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import static com.github.ferstl.skydemonexporter.EnvironmentUtil.expandUserHome;
import static com.github.ferstl.skydemonexporter.EnvironmentUtil.readAirfieldNames;

public class ExporterRunner implements Callable<Integer> {

  @Option(
      names = {"-h", "--help"},
      usageHelp = true,
      description = "Display this help message"
  )
  private boolean helpRequested;

  @Option(
      names = {"-d", "--data"},
      paramLabel = "DIRECTORY",
      description = "Directory containing the SkyDemon files and the index.xml"
  )
  private String platesDir;

  @Parameters(
      paramLabel = "TARGET",
      description = "Directory to export the data. It will be created if it does not exist.")
  private String targetDir;

  public static void main(String[] args) throws Exception {
    int status = new CommandLine(new ExporterRunner()).execute(args);
    System.exit(status);
  }

  @Override
  public Integer call() throws Exception {
    Path platesDir = (this.platesDir != null) ? Path.of(expandUserHome(this.platesDir)) : EnvironmentUtil.findSkyDemonData();
    Path targetDir = Path.of(expandUserHome(this.targetDir));
    Properties airfieldNames = readAirfieldNames();

    new Exporter(airfieldNames, platesDir, targetDir).export();
    return 0;
  }

}
