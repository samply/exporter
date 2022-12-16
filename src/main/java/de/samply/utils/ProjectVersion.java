package de.samply.utils;

import de.samply.teiler.TeilerConst;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectVersion {

  private final static String POM_FILE = "pom.xml";

  private final static Logger logger = LoggerFactory.getLogger(ProjectVersion.class);

  public static String getProjectVersion() {
    try {
      return getProjectVersion_WithoutManagementException();
    } catch (IOException | XmlPullParserException e) {
      logger.error("Error getting project version", e);
      return TeilerConst.APP_NAME;
    }
  }

  private static String getProjectVersion_WithoutManagementException()
      throws IOException, XmlPullParserException {

    if (Files.exists(Paths.get(POM_FILE))) {
      return getProjectVersionFromPomFile();
    } else {
      return getProjectVersionFromManifest();
    }

  }

  private static String getProjectVersionFromPomFile() throws IOException, XmlPullParserException {

    MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
    Model model = mavenXpp3Reader.read(new FileReader(POM_FILE));

    return fetchVersion(model);

  }

  private static String getProjectVersionFromManifest() {

    String implementationVersion = ProjectVersion.class.getPackage().getImplementationVersion();
    String implementationTitle = ProjectVersion.class.getPackage().getImplementationTitle();

    return createVersionToDisplay(implementationTitle, implementationVersion);

  }

  private static String fetchVersion(Model model) {
    return createVersionToDisplay(model.getArtifactId(), model.getVersion());
  }

  public static String createVersionToDisplay(String artifactId, String version) {
    return artifactId + ':' + version;
  }

}
