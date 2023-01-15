package de.samply.zip;

import de.samply.teiler.TeilerConst;
import de.samply.template.ConverterTemplateUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
public class Zipper {

  private ConverterTemplateUtils converterTemplateUtils;
  private String defaultZipFilename;
  private Path temporalFileDirectory;

  public Zipper(
      @Autowired ConverterTemplateUtils converterTemplateUtils,
      @Value(TeilerConst.ZIP_FILENAME_SV) String defaultZipFilename,
      @Value(TeilerConst.TEMPORAL_FILE_DIRECTORY_SV) String temporalFileDirectory) {
    this.converterTemplateUtils = converterTemplateUtils;
    this.defaultZipFilename = defaultZipFilename;
    this.temporalFileDirectory = Path.of(temporalFileDirectory);
  }

  public Pair<InputStreamResource, String> zipFiles(List<String> filePaths)
      throws ZipperException {
    return zipFiles(temporalFileDirectory.resolve(generateZipFilename()), filePaths);
  }

  public Pair<InputStreamResource, String> zipFiles(Path zipFilePath, List<String> filePaths)
      throws ZipperException {
    try {
      return Pair.of(zipFilesWithoutExceptionManagement(zipFilePath, filePaths),
          zipFilePath.getFileName().toString());
    } catch (FileNotFoundException e) {
      throw new ZipperException(e);
    }
  }

  private InputStreamResource zipFilesWithoutExceptionManagement(Path zipFilePath,
      List<String> filePaths) throws FileNotFoundException, ZipperException {
    try (FileOutputStream outputStream = new FileOutputStream(
        zipFilePath.toFile()); ZipOutputStream zipOutputStream = new ZipOutputStream(
        outputStream)) {
      addFilesToZipOutputStream(zipOutputStream, filePaths);
      return new InputStreamResource(new FileInputStream(zipFilePath.toFile()));
    } catch (IOException e) {
      throw new ZipperException(e);
    }
  }

  private void addFilesToZipOutputStream(ZipOutputStream zipOutputStream, List<String> filePaths)
      throws ZipperException {
    try {
      filePaths.forEach(filePath -> {
        File fileToZip = new File(filePath);
        try (FileInputStream fileInputStream = new FileInputStream(fileToZip)) {
          ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
          zipOutputStream.putNextEntry(zipEntry);
          byte[] bytes = new byte[1024];
          int length;
          while ((length = fileInputStream.read(bytes)) >= 0) {
            zipOutputStream.write(bytes, 0, length);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (RuntimeException e) {
      throw new ZipperException(e);
    }
  }

  public String generateZipFilename() {
    return converterTemplateUtils.replaceTokens(defaultZipFilename);
  }

}