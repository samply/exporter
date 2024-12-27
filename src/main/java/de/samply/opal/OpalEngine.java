package de.samply.opal;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.samply.exporter.ExporterConst;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.template.ContainerTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

public class OpalEngine {

    private final WebClient webClient;
    private final OpalServer opalServer;
    private final static Logger logger = BufferedLoggerFactory.getLogger(OpalEngine.class);

    public OpalEngine(OpalServer opalServer) {
        this.opalServer = opalServer;
        this.webClient = opalServer.createWebClient();
    }

    public void sendPathToOpal(Path path, Session session) throws OpalEngineException, JsonProcessingException {
        ContainerTemplate containerTemplate = session.getContainerTemplate(
                path.getFileName().toString());
        if (containerTemplate != null) {
            uploadPath(path, session);
            String uid = importPathTransient(path, session, containerTemplate);
            String tasklocation = importPath(session, containerTemplate, uid);
            waitUntilTaskIsFinished(tasklocation);
            createView(session, containerTemplate);
            deletePath(path, session);
        }
    }

    public void createProjectIfNotExists(Session session) throws OpalEngineException, JsonProcessingException {
        if (!existsProject(session)) {
            createProject(session);
            createProjectPermissions(session);
        }
    }

    private boolean existsProject(Session session) {
        return Boolean.TRUE.equals(webClient.get()
                .uri(ExporterConst.OPAL_PROJECT + session.fetchProject())
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
    }

    private void createProject(Session session) throws JsonProcessingException {
        String projectName = session.fetchProject();
        logger.info("Create Project " + projectName);
        webClient.post()
                .uri(ExporterConst.OPAL_PROJECT_WS + ExporterConst.PROJECTS_OPAL)
                .headers(headers -> headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .bodyValue(OpalClientBodyFactory.createProjectBodyAndSerializeAsJson(projectName, opalServer.getDatabase()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(String.class)
                .block();
    }

    private void createProjectPermissions(Session session) {
        List<String> results = new ArrayList<>();
        String subjects = session.getConverterTemplate().getOpalPermissionSubjects();
        if (subjects != null && !subjects.isEmpty()) {
            Arrays.stream(subjects.trim().split(ExporterConst.OPAL_PERMISSION_SUBJECT_SEPARATOR)).forEach(subject -> {
                logger.info("Create Project Permission");
                results.add(webClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path(ExporterConst.OPAL_PROJECT + session.fetchProject() + ExporterConst.OPAL_PROJECT_PERM)
                                .queryParam("type", session.getConverterTemplate().getOpalPermissionType().toString())
                                .queryParam("permission", session.getConverterTemplate().getOpalPermission().toString())
                                .queryParam("principal", subject)
                                .build())
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                        .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                        .bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .block());
            });
        }
    }

    private void uploadPath(Path path, Session session) {
        logger.info("Uploading file: " + path.getFileName());
        webClient.post()
                .uri(ExporterConst.OPAL_PROJECT_WS + ExporterConst.OPAL_PROJECT_FILE + session.fetchProject())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(new LinkedMultiValueMap<>() {{
                    add("file", new FileSystemResource(path.toFile()));
                }})
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(String.class)
                .block();
        logger.info("File uploaded successfully.");
    }

    private void deletePath(Path path, Session session) {
        logger.info("Attempting to delete file at path: " + path);
        webClient.delete()
                .uri(ExporterConst.OPAL_PROJECT_FILES + fetchOpalProjectDirectoryPath(session, path))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .toBodilessEntity()
                .block();
        logger.info("File successfully deleted.");
    }

    private String fetchOpalProjectDirectoryPath(Session session, Path path){
        return session.fetchOpalProjectDirectoryPath(opalServer.getFilesDirectory(), path);
    }

    private String importPathTransient(Path path, Session session, ContainerTemplate template) throws OpalEngineException, JsonProcessingException {
        logger.info("Import started for: " + template.getOpalTable() + " in Project " + session.fetchProject());
        Map<String, Object> response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ExporterConst.OPAL_PROJECT + session.fetchProject() + ExporterConst.OPAL_PROJECT_TRS)
                        .queryParam("merge", false)
                        .build())
                .headers(headers -> {
                    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .bodyValue(OpalClientBodyFactory.createCsvDatasourceBodyAndSerializeAsJson(session, template,
                        fetchOpalProjectDirectoryPath(session, path)))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
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

    private String importPath(Session session, ContainerTemplate template, String transientUid) throws JsonProcessingException {
        return webClient.post()
                .uri(ExporterConst.OPAL_PROJECT + session.fetchProject() + ExporterConst.OPAL_PROJECT_IMPORT)
                .headers(headers -> headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .bodyValue(OpalClientBodyFactory.createPathBodyAndSerializeAsJson(session, template, transientUid))
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
    }

    public void waitUntilTaskIsFinished(String taskId) throws OpalEngineException {
        if (taskId != null) {

            int maxRetries = 10;
            Duration retryDelay = Duration.ofSeconds(2);

            Flux.interval(retryDelay)
                    .take(maxRetries)
                    .flatMap(attempt -> webClient.get()
                            .uri(taskId)
                            .headers(headers -> headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                            .retrieve()
                            .bodyToMono(Map.class)
                            .onErrorResume(e -> {
                                System.err.println("Error when retrieving the task status: " + e.getMessage());
                                return Mono.empty();
                            })
                    )
                    .filter(Objects::nonNull)
                    .takeUntil(response -> {
                        String status = (String) response.get("status");
                        return "SUCCEEDED".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status);
                    })
                    .last()
                    .flatMap(response -> {
                        String status = (String) response.get("status");
                        if ("SUCCEEDED".equalsIgnoreCase(status)) {
                            return Mono.just(true);
                        } else if ("FAILED".equalsIgnoreCase(status)) {
                            List<Map<String, Object>> messages = (List<Map<String, Object>>) response.get("messages");
                            String errorMessage = messages != null
                                    ? messages.stream()
                                    .map(msg -> (String) msg.get("msg"))
                                    .reduce((a, b) -> a + "; " + b)
                                    .orElse("Unknown error")
                                    : "Unknown error";
                            return Mono.error(new RuntimeException("Task failed: " + errorMessage));
                        }
                        return Mono.error(new RuntimeException("Unexpected task status"));
                    })
                    .switchIfEmpty(Mono.error(new RuntimeException("Task was not completed within the expected time")))
                    .block();
        } else {
            throw new OpalEngineException("Task ID not found");
        }
    }

    private void createView(Session session, ContainerTemplate containerTemplate)
            throws OpalEngineException {
        try {
            createViewWithoutExceptionHandling(session, containerTemplate);
        } catch (JsonProcessingException e) {
            throw new OpalEngineException(e);
        }
    }

    private void createViewWithoutExceptionHandling(Session session, ContainerTemplate containerTemplate) throws JsonProcessingException {
        String view = OpalClientBodyFactory.createViewAndSerializeAsJson(session, containerTemplate);
        logger.info("Create View");
        logger.info("Request body: " + view);
        webClient.post()
                .uri(ExporterConst.OPAL_PROJECT_WS + OpalUtils.createViewsPath(session))
                .headers(headers -> {
                    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .bodyValue(view)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(String.class)
                .block();
    }

    private Mono<? extends Throwable> handleError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(body -> {
                    logger.error(body);
                    return Mono.error(new OpalEngineException(body));
                });
    }

}
