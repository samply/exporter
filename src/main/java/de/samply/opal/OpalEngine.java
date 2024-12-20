package de.samply.opal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.samply.exporter.ExporterConst;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.opal.model.View;
import de.samply.template.ContainerTemplate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class OpalEngine {

    private final WebClient webClient;
    private final OpalServer opalServer;
    private final static Logger logger = BufferedLoggerFactory.getLogger(OpalEngine.class);
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule());

    public OpalEngine(OpalServer opalServer) {
        this.opalServer = opalServer;
        this.webClient = opalServer.createWebClient();
    }

    public void sendPathToOpal(Path path, Session session) throws OpalEngineException {
        ContainerTemplate containerTemplate = session.getContainerTemplate(
                path.getFileName().toString());
        if (containerTemplate != null) {
            uploadPath(path, session);
            String uid = importPath_transient(path, session, containerTemplate);
            String tasklocation = importPath(session, containerTemplate, uid);
            if (waitUntilTaskIsFinished(tasklocation)){
            createView(session, containerTemplate);}
            deletePath(path, session);
        }
    }

    public void createProjectIfNotExists(Session session) throws OpalEngineException {
        if (!existsProject(session)) {
            createProject(session);
            createProjectPermissions(session);
        }
    }

    private boolean existsProject(Session session) throws OpalEngineException {
        String url = ExporterConst.OPAL_PROJECT + session.fetchProject();
        try {
            return webClient.get()
                    .uri(url)
                    .headers(headers -> headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return Mono.just(true);
                        }
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    logger.info(body);
                                    return Mono.just(false);
                                });
                    })
                    .onErrorMap(e -> new OpalEngineException(e))
                    .block();
        } catch (Exception e) {
            throw new OpalEngineException(e);
        }
    }

    private String createProject(Session session) throws OpalEngineException {
        String url = ExporterConst.OPAL_PROJECT_WS + ExporterConst.PROJECTS_OPAL;
        String projectName = session.fetchProject();
        String database = opalServer.getDatabase();

        String requestBody = "{\n" +
                "    \"name\": \"" + projectName + "\",\n" +
                "    \"title\": \"" + projectName + "\",\n" +
                "    \"description\": \"\",\n" +
                "    \"database\": \"" + database + "\",\n" +
                "    \"vcfStoreService\": null,\n" +
                "    \"exportFolder\": \"\"\n" +
                "}";
        try{
            return webClient.post()
                    .uri(url)
                    .headers(headers -> headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), this::handleError)
                    .onStatus(status -> status.is5xxServerError(), this::handleError)
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new OpalEngineException(e);
        }
    }

    private List<String> createProjectPermissions(Session session) throws OpalEngineException {
        List<String> results = new ArrayList<>();
        String subjects = session.getConverterTemplate().getOpalPermissionSubjects();
        if (subjects != null && subjects.length() > 0) {
            for (String subject : subjects.trim()
                    .split(ExporterConst.OPAL_PERMISSION_SUBJECT_SEPARATOR)) {
                String baseUrl = ExporterConst.OPAL_PROJECT + session.fetchProject() + ExporterConst.OPAL_PROJECT_PERM;

                try {
                    String responseBody = webClient.post()
                            .uri(uriBuilder -> uriBuilder
                                    .path(baseUrl)
                                    .queryParam("type", session.getConverterTemplate().getOpalPermissionType().toString())
                                    .queryParam("permission", session.getConverterTemplate().getOpalPermission().toString())
                                    .queryParam("principal", subject)
                                    .build())
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError(), this::handleError)
                            .onStatus(status -> status.is5xxServerError(), this::handleError)
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .block();

                    results.add(responseBody);
                } catch (Exception e) {
                    throw new OpalEngineException(e);
                }
            }
        }
        return results;
    }

    private String uploadPath(Path path, Session session) throws OpalEngineException {
        String url = ExporterConst.OPAL_PROJECT_WS + ExporterConst.OPAL_PROJECT_FILE + session.fetchProject();
        FileSystemResource fileResource = new FileSystemResource(path.toFile());
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        try{
            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), this::handleError)
                    .onStatus(status -> status.is5xxServerError(), this::handleError)
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new OpalEngineException(e);
        }
    }

    private ResponseEntity<Void> deletePath(Path path, Session session) throws OpalEngineException {
        String url = ExporterConst.OPAL_PROJECT_FILES + session.fetchOpalProjectDirectoryPath(opalServer.getFilesDirectory(), path);

        try{
            return webClient.delete()
                    .uri(url)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), this::handleError)
                    .onStatus(status -> status.is5xxServerError(), this::handleError)
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new OpalEngineException(e);
        }
    }

    private String importPath_transient(Path path, Session session, ContainerTemplate template)
            throws OpalEngineException {
        String url_transient_ds = ExporterConst.OPAL_PROJECT + session.fetchProject() + ExporterConst.OPAL_PROJECT_TRS;
        String characterSet = "ISO-8859-1";
        int firstRow = 1;
        String quote = "\"";
        String defaultValueType = "text";
        Map<String, Object> requestBody = Map.of(
                "Magma.CsvDatasourceFactoryDto.params", Map.of(
                        "characterSet", characterSet,
                        "firstRow", firstRow,
                        "quote", quote,
                        "separator", normalizeSepartor(session.getConverterTemplate().getCsvSeparator()),
                        "defaultValueType", defaultValueType,
                        "tables", new Object[]{
                                Map.of(
                                        "name", template.getOpalTable(),
                                        "data", session.fetchOpalProjectDirectoryPath(opalServer.getFilesDirectory(), path),
                                        "entityType", template.getOpalEntityType(),
                                        "refTable", session.fetchProject() + "." + template.getOpalTable()
                                )
                        }
                )
        );

        Map<String, Object> response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(url_transient_ds)
                        .queryParam("merge", false)
                        .build())
                .headers(headers -> {
                    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), this::handleError)
                .onStatus(status -> status.is5xxServerError(), this::handleError)
                .bodyToMono(Map.class)
                .block();

        String transientUid = (String) response.get("name");
        if (transientUid == null) {
            throw new OpalEngineException("UID for the transient table could not be extracted");
        }
        return transientUid;
    }

    private String importPath(Session session, ContainerTemplate template, String transientUid)
            throws OpalEngineException {
        String url_ds = ExporterConst.OPAL_PROJECT + session.fetchProject() + ExporterConst.OPAL_PROJECT_IMPORT;
        Map<String, Object> requestBody = Map.of(
                "destination", session.fetchProject(),
                "tables", new String[]{transientUid + "." + template.getOpalTable()}
        );

        try{
            return webClient.post()
                    .uri(url_ds)
                    .headers(headers -> headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), this::handleError)
                    .onStatus(status -> status.is5xxServerError(), this::handleError)
                    .toBodilessEntity()
                    .map(response -> {
                        String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
                        if (location == null) {
                            return null;
                            //throw new OpalEngineException("Location-Header fehlt in der Antwort");
                        }
                        return location;
                    })
                    .block();
        } catch (Exception e) {
            throw new OpalEngineException(e);
        }
    }

    private Boolean waitUntilTaskIsFinished(String taskId) throws OpalEngineException {
        if (taskId != null) {
            int maxRetries = 10;
            int retryDelayMs = 2000;

            for (int i = 0; i < maxRetries; i++) {
                Map<String, Object> taskResponse = webClient.get()
                        .uri(taskId)
                        .headers(headers -> headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError(), this::handleError)
                        .onStatus(status -> status.is5xxServerError(), this::handleError)
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .onErrorResume(e -> {
                            System.err.println("Error when retrieving the task status: " + e.getMessage());
                            return Mono.empty();
                        })
                        .block();

                if (taskResponse != null) {
                    String status = (String) taskResponse.get("status");

                    if ("SUCCEEDED".equalsIgnoreCase(status)) {
                        return true;
                    } else if ("FAILED".equalsIgnoreCase(status)) {
                        List<Map<String, Object>> messages = (List<Map<String, Object>>) taskResponse.get("messages");
                        String errorMessage = messages != null ? extractErrorMessage(messages) : "Unknown error";
                        throw new OpalEngineException("Task failed: " + errorMessage);
                    }

                    List<Map<String, Object>> messages = (List<Map<String, Object>>) taskResponse.get("messages");
                    if (messages != null) {
                        messages.forEach(msg -> System.out.println("Task message: " + msg.get("msg")));
                    }
                }

                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new OpalEngineException("Task polling interrupted", e);
                }
            }

            throw new OpalEngineException("Task " + taskId + " was not completed within the expected time.");
        } else {
            throw new OpalEngineException("Task " + taskId + " not found");
        }
    }

    private String extractErrorMessage(List<Map<String, Object>> messages) {
        return messages.stream()
                .map(msg -> (String) msg.get("msg"))
                .reduce((first, second) -> first + "; " + second)
                .orElse("No specific message found");
    }

    private String createView(Session session, ContainerTemplate containerTemplate)
            throws OpalEngineException {
        try {
            return createViewWithoutExceptionHandling(session, containerTemplate);
        } catch (JsonProcessingException e) {
            throw new OpalEngineException(e);
        }
    }

    private String createViewWithoutExceptionHandling(Session session,
                                                      ContainerTemplate containerTemplate)
            throws JsonProcessingException, OpalEngineException {
        View view = ViewFactory.createView(session, containerTemplate);
        String sView = objectMapper.writeValueAsString(view);
        String suboperation = ExporterConst.OPAL_PROJECT_WS + OpalUtils.createViewsPath(session);

        try{
            return webClient.post()
                    .uri(suboperation)
                    .headers(headers -> {
                        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    })
                    .bodyValue(sView)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), this::handleError)
                    .onStatus(status -> status.is5xxServerError(), this::handleError)
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new OpalEngineException(e);
        }
    }

    private String normalizeSepartor(String separator) {
        return separator.replace("\t", "\\t");
    }

    private Mono<? extends Throwable> handleError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(body -> {
                    logger.error(body);
                    return Mono.error(new OpalEngineException(body));
                });
    }

}