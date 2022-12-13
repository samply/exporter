package de.samply.result.container.write;

import de.samply.result.container.Container;
import de.samply.result.container.Containers;
import de.samply.result.container.template.ContainerTemplate;
import de.samply.result.container.template.ContainersTemplate;
import de.samply.teiler.TeilerConst;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContainersCsvWriter {

  private String writeDirectory;

  public ContainersCsvWriter(@Value(TeilerConst.WRITE_FILE_DIRECTORY_SV) String writeDirectory) {
    this.writeDirectory = writeDirectory;
  }

  public void writeContainersInCsv(Containers containers) {
    containers.getContainersTemplate().getContainerTemplates().forEach(containerTemplate ->
        writeContainersInCsv(containers.getContainers(containerTemplate),
            containers.getContainersTemplate(), containerTemplate)
    );
  }

  private void writeContainersInCsv(List<Container> containers,
      ContainersTemplate containersTemplate, ContainerTemplate containerTemplate) {
    try {
      Path filePath = getFilePath(containerTemplate.getCsvFilename());
      boolean headersExists = headersExists(filePath);
      Files.write(filePath,
          new ContainerWriterIterable(containers, containersTemplate, containerTemplate,
            headersExists  ),
          (headersExists) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Path getFilePath(String filename) {
    return Paths.get(writeDirectory).resolve(filename);
  }

  private boolean headersExists(Path filePath) {
    return Files.exists(filePath) && filePath.toFile().length() > 0;
  }

}
