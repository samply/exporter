package de.samply.fhir;

import de.samply.exporter.ExporterConst;
import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;
import org.hl7.fhir.utilities.npm.NpmPackage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class FhirPackageLoader {

    private final String fhirPackageDirectory;

    public FhirPackageLoader(String fhirPackageDirectory) {
        this.fhirPackageDirectory = fhirPackageDirectory;
    }

    public void loadPackage(NpmPackageValidationSupport npmPackageSupport, String fhirPackage)
            throws IOException {
        loadPackage(npmPackageSupport, fetchNpmPackage(fhirPackage));
    }

    private String createFhirPackagePath(String fhirPackage) throws IOException {
        return (fhirPackage.toLowerCase().contains("http")) ? downloadFhirPackageAndGetPath(fhirPackage)
                : fetchFhirPackagePath(fhirPackage);
    }

    private String downloadFhirPackageAndGetPath(String fhirPackage) throws IOException {
        String result = fetchFhirPackagePath(fetchFilenameFromUrl(fhirPackage));
        try (InputStream inputStream = new URL(fhirPackage).openStream()) {
            copyInputStreamToFilePath(inputStream, result);
        }
        return result;
    }

    private void copyInputStreamToFilePath(InputStream inputStream, String filePath) throws IOException {
        try (
                ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
                FileOutputStream fileOutputStream = new FileOutputStream(filePath)
        ) {
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }


    /*
      private String downloadFhirPackageAndGetPath(String fhirPackageUrl) throws IOException {
        WebClient webClient = WebClient.builder().build();
        AtomicReference<String> filePath = new AtomicReference();
        byte[] output = webClient.get().uri(fhirPackageUrl).exchangeToMono(clientResponse -> {
          List<String> header = clientResponse.headers()
              .header(ExporterConst.HTTP_HEADER_CONTENT_DISPOSITION);
          String filename =
              (header != null && header.size() > 0) ? fetchFilenameFromHeader(header.get(0))
                  : fetchRandomFilename();
          filePath.set(fetchFhirPackagePath(filename));
          return clientResponse.bodyToMono(byte[].class);
        }).block();
        try (InputStream inputStream = new ByteArrayInputStream(output)){
          copyInputStreamToFilePath(inputStream, filePath.get());
        }
        return filePath.get();
      }
    */
    private String fetchFhirPackagePath(String filename) {
        return Path.of(fhirPackageDirectory).resolve(filename).toString();
    }

    private String fetchFilenameFromUrl(String fileUrl) {
        try {
            URLConnection urlConnection = new URL(fileUrl).openConnection();
            String headerField = urlConnection.getHeaderField(
                    ExporterConst.HTTP_HEADER_CONTENT_DISPOSITION);
            return fetchFilenameFromHeader(headerField);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String fetchFilenameFromHeader(String headerField) {
        return (headerField != null && headerField.contains(
                ExporterConst.HTTP_HEADER_CONTENT_DISPOSITION_FILENAME)) ?
                headerField.substring(
                        headerField.indexOf(ExporterConst.HTTP_HEADER_CONTENT_DISPOSITION_FILENAME)
                                + ExporterConst.HTTP_HEADER_CONTENT_DISPOSITION_FILENAME.length()) : fetchRandomFilename();
    }

    private String fetchRandomFilename() {
        return RandomStringUtils.random(ExporterConst.RANDOM_FILENAME_SIZE, true, false) + ".tgz";
    }

    private NpmPackage fetchNpmPackage(String fhirPackage) throws IOException {
        return fetchNpmPackage(Path.of(createFhirPackagePath(fhirPackage)));
    }

    private NpmPackage fetchNpmPackage(Path fhirPackagePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(fhirPackagePath)) {
            return NpmPackage.fromPackage(inputStream);
        }
    }

    private void loadPackage(NpmPackageValidationSupport npmPackageSupport, NpmPackage npmPackage) throws IOException {
        try {
            loadPackageWithoutExceptionHandling(npmPackageSupport, npmPackage);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IOException(e);
        }
    }

    private void loadPackageWithoutExceptionHandling(NpmPackageValidationSupport npmPackageSupport, NpmPackage npmPackage) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Caution: This is evil!
        Method loadResourcesFromPackage = fetchLoadResourceFromPackageMethod();
        loadResourcesFromPackage.invoke(npmPackageSupport, npmPackage);
    }

    private Method fetchLoadResourceFromPackageMethod() throws NoSuchMethodException {
        for (Method method : NpmPackageValidationSupport.class.getDeclaredMethods()) {
            if (method.getName().equals("loadResourcesFromPackage")) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException();
    }

}
