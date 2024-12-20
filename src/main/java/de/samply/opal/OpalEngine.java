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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
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
            String uid = importPathTransient(path, session, containerTemplate);
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
            return Boolean.TRUE.equals(webClient.get()
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
                    .onErrorMap(OpalEngineException::new)
                    .block());
        } catch (Exception e) {
            throw new OpalEngineException(e);
        }
    }

    private void createProject(Session session) throws OpalEngineException {
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
        logger.info("Create Project " + projectName);
        try{
            webClient.post()
                    .uri(url)
                    .headers(headers -> headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new OpalEngineException(e);
        }
    }

    private void createProjectPermissions(Session session) throws OpalEngineException {
        List<String> results = new ArrayList<>();
        String subjects = session.getConverterTemplate().getOpalPermissionSubjects();
        if (subjects != null && !subjects.isEmpty()) {
            for (String subject : subjects.trim()
                    .split(ExporterConst.OPAL_PERMISSION_SUBJECT_SEPARATOR)) {
                String baseUrl = ExporterConst.OPAL_PROJECT + session.fetchProject() + ExporterConst.OPAL_PROJECT_PERM;

                logger.info("Create Project Permission");
                try {
                    String responseBody = webClient.post()
                            .uri(uriBuilder -> uriBuilder
                                    .path(baseUrl)
                                    .queryParam("type", session.getConverterTemplate().getOpalPermissionType().toString())
                                    .queryParam("permission", session.getConverterTemplate().getOpalPermission().toString())
                                    .queryParam("principal", subject)
                                    .build())
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                            .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .block();

                    results.add(responseBody);
                } catch (Exception e) {
                    throw new OpalEngineException(e);
                }
            }
        }
    }

    private void uploadPath(Path path, Session session) throws OpalEngineException {
        String url = ExporterConst.OPAL_PROJECT_WS + ExporterConst.OPAL_PROJECT_FILE + session.fetchProject();
        logger.info("Starting file upload. File path: " +  path.toString());
        FileSystemResource fileResource = new FileSystemResource(path.toFile());
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        try{
            logger.info("Uploading file: " + path.getFileName());
            webClient.post()
                    .uri(url)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                    .bodyToMono(String.class)
                    .block();
            logger.info("File uploaded successfully.");
        } catch (Exception e) {
            throw new OpalEngineException(e);
        }
    }

    private void deletePath(Path path, Session session) throws OpalEngineException {
        String url = ExporterConst.OPAL_PROJECT_FILES + session.fetchOpalProjectDirectoryPath(opalServer.getFilesDirectory(), path);
        logger.info("Attempting to delete file at path: " + path);
        try{
            webClient.delete()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                    .toBodilessEntity()
                    .block();
            logger.info("File successfully deleted.");
        } catch (Exception e) {
            throw new OpalEngineException(e);
        }
    }

    private String importPathTransient(Path path, Session session, ContainerTemplate template)
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
        logger.info("Import started for: " + template.getOpalTable() + " in Project " + session.fetchProject());
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
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (response == null) {
            throw new IllegalArgumentException("Response must not be null");
        }
        String transientUid = (String) response.get("name");
        if (transientUid == null) {
            throw new OpalEngineException("UID for the transient table could not be extracted");
        }
        logger.info("Extracted transient UID: " + transientUid);
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
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                    .toBodilessEntity()
                    .mapNotNull(response -> {
                        String locationHeader = response.getHeaders().getFirst(HttpHeaders.LOCATION);
                        if (locationHeader != null) {
                            logger.info("Received Location header: " + locationHeader);
                        }
                        return locationHeader;
                    })
                    .onErrorMap(e -> new OpalEngineException("Error when processing the response", e))
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
                        .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                        .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
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
            throw new OpalEngineException("Task ID not found");
        }
    }

    private String extractErrorMessage(List<Map<String, Object>> messages) {
        return messages.stream()
                .map(msg -> (String) msg.get("msg"))
                .reduce((first, second) -> first + "; " + second)
                .orElse("No specific message found");
    }

    private void createView(Session session, ContainerTemplate containerTemplate)
            throws OpalEngineException {
        try {
            createViewWithoutExceptionHandling(session, containerTemplate);
        } catch (JsonProcessingException e) {
            throw new OpalEngineException(e);
        }
    }

    private void createViewWithoutExceptionHandling(Session session,
                                                    ContainerTemplate containerTemplate)
            throws JsonProcessingException, OpalEngineException {
        View view = ViewFactory.createView(session, containerTemplate);
        String sView = objectMapper.writeValueAsString(view);
        String suboperation = ExporterConst.OPAL_PROJECT_WS + OpalUtils.createViewsPath(session);
        logger.info("Create View");
        logger.info("Request body: " + sView);
        try{
            webClient.post()
                    .uri(suboperation)
                    .headers(headers -> {
                        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    })
                    .bodyValue(sView)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
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