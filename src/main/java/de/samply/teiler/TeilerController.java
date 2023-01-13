package de.samply.teiler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.samply.converter.Format;
import de.samply.core.TeilerCore;
import de.samply.core.TeilerCoreException;
import de.samply.core.TeilerCoreParameters;
import de.samply.core.TeilerParameters;
import de.samply.db.crud.TeilerDbService;
import de.samply.db.model.Query;
import de.samply.db.model.QueryExecution;
import de.samply.db.model.QueryExecutionError;
import de.samply.db.model.QueryExecutionFile;
import de.samply.db.model.Status;
import de.samply.teiler.response.entity.CreateQueryResponseEntity;
import de.samply.teiler.response.entity.RequestResponseEntity;
import de.samply.utils.ProjectVersion;
import de.samply.zip.Zipper;
import de.samply.zip.ZipperException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Flux;

@RestController
public class TeilerController {

  private final static Logger logger = LoggerFactory.getLogger(TeilerController.class);

  private ObjectMapper objectMapper = new ObjectMapper()
      .enable(SerializationFeature.INDENT_OUTPUT)
      .registerModule(new JavaTimeModule());
  private final String projectVersion = ProjectVersion.getProjectVersion();
  private final TeilerCore teilerCore;
  private TeilerDbService teilerDbService;
  private Zipper zipper;

  public TeilerController(
      @Autowired TeilerCore teilerCore,
      @Autowired TeilerDbService teilerDbService,
      @Autowired Zipper zipper) {
    this.teilerCore = teilerCore;
    this.teilerDbService = teilerDbService;
    this.zipper = zipper;
  }

  @GetMapping(value = TeilerConst.INFO)
  public ResponseEntity<String> info() {
    return new ResponseEntity<>(projectVersion, HttpStatus.OK);
  }

  //TODO: Add , produces = MediaType.APPLICATION_JSON_VALUE
  @PostMapping(value = TeilerConst.CREATE_QUERY)
  public ResponseEntity<String> createQuery(
      @RequestParam(name = TeilerConst.QUERY) String query,
      @RequestParam(name = TeilerConst.QUERY_FORMAT) Format queryFormat,
      @RequestParam(name = TeilerConst.QUERY_LABEL) String queryLabel,
      @RequestParam(name = TeilerConst.QUERY_DESCRIPTION) String queryDescription,
      @RequestParam(name = TeilerConst.QUERY_CONTACT_ID) String queryContactId,
      @RequestParam(name = TeilerConst.QUERY_EXPIRATION_DATE, required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryExpirationDate
  ) {
    Query tempQuery = new Query();
    tempQuery.setQuery(query);
    tempQuery.setFormat(queryFormat);
    tempQuery.setLabel(queryLabel);
    tempQuery.setDescription(queryDescription);
    tempQuery.setContactId(queryContactId);
    tempQuery.setExpirationDate(queryExpirationDate);
    tempQuery.setCreatedAt(Instant.now());
    Long queryId = teilerDbService.saveQueryAndGetQueryId(tempQuery);

    try {
      return ResponseEntity.ok(createCreateQueryResponseEntity(queryId));
    } catch (TeilerControllerException e) {
      return createInternalServerError(e);
    }
  }

  private String createCreateQueryResponseEntity(Long queryId) throws TeilerControllerException {
    try {
      return objectMapper.writeValueAsString(new CreateQueryResponseEntity(queryId));
    } catch (JsonProcessingException e) {
      throw new TeilerControllerException(e);
    }
  }

  @GetMapping(value = TeilerConst.FETCH_QUERIES, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getQueries(
      @RequestParam(name = TeilerConst.PAGE, required = false) Integer page,
      @RequestParam(name = TeilerConst.PAGE_SIZE, required = false) Integer pageSize
  ) {
    if (page == null && pageSize == null) {
      return fetchAllEntities();
    } else if (page != null && pageSize != null) {
      return fetchAllEntities(page, pageSize);
    } else {
      return ResponseEntity.badRequest()
          .body((page == null) ? "Page not provided" : "Page size not provided");
    }
  }

  private ResponseEntity fetchAllEntities() {
    try {
      return fetchAllEntitiesWithoutExceptionManagement();
    } catch (JsonProcessingException e) {
      return createInternalServerError(e);
    }
  }

  private ResponseEntity createInternalServerError(Exception e) {
    return ResponseEntity.internalServerError().body(ExceptionUtils.getStackTrace(e));
  }

  private ResponseEntity fetchAllEntitiesWithoutExceptionManagement()
      throws JsonProcessingException {
    List<Query> queries = teilerDbService.fetchAllQueries();
    String result = objectMapper.writeValueAsString(queries);
    return ResponseEntity.ok(result);
  }

  private ResponseEntity fetchAllEntities(int page, int pageSize) {
    try {
      return fetchAllEntitiesWithoutExceptionManagement(page, pageSize);
    } catch (JsonProcessingException e) {
      return createInternalServerError(e);
    }
  }

  private ResponseEntity fetchAllEntitiesWithoutExceptionManagement(int page, int pageSize)
      throws JsonProcessingException {
    List<Query> queries = teilerDbService.fetchAllQueries(page, pageSize);
    String result = objectMapper.writeValueAsString(queries);
    return ResponseEntity.ok(result);
  }

  @PostMapping(value = TeilerConst.REQUEST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> postRequest(
      HttpServletRequest httpServletRequest,
      @RequestParam(name = TeilerConst.QUERY_ID, required = false) Long queryId,
      @RequestParam(name = TeilerConst.QUERY, required = false) String query,
      @RequestParam(name = TeilerConst.SOURCE_ID) String sourceId,
      @RequestParam(name = TeilerConst.QUERY_FORMAT, required = false) Format queryFormat,
      @RequestParam(name = TeilerConst.QUERY_LABEL, required = false) String queryLabel,
      @RequestParam(name = TeilerConst.QUERY_DESCRIPTION, required = false) String queryDescription,
      @RequestParam(name = TeilerConst.QUERY_CONTACT_ID, required = false) String queryContactId,
      @RequestParam(name = TeilerConst.QUERY_EXPIRATION_DATE, required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryExpirationDate,
      @RequestParam(name = TeilerConst.OUTPUT_FORMAT) Format outputFormat,
      @RequestParam(name = TeilerConst.TEMPLATE_ID, required = false) String templateId,
      @RequestHeader(name = "Content-Type", required = false) String contentType,
      @RequestBody(required = false) String template
  ) {
    if (!outputFormat.isPath()) {
      return ResponseEntity.badRequest().body("Output format is not a file");
    }
    TeilerCoreParameters teilerCoreParameters = null;
    try {
      teilerCoreParameters = teilerCore.extractParameters(
          new TeilerParameters(queryId, query, sourceId, templateId, template, contentType,
              queryFormat, queryLabel, queryDescription, queryContactId, queryExpirationDate,
              outputFormat));
    } catch (TeilerCoreException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
    Long queryExecutionId = teilerDbService.saveQueryExecutionAndGetExecutionId(
        createQueryExecution(teilerCoreParameters));
    final TeilerCoreParameters tempTeilerCoreParameters = teilerCoreParameters;
    new Thread(() -> generateFiles(tempTeilerCoreParameters, queryExecutionId)).start();
    try {
      return ResponseEntity.ok()
          .body(createRequestResponseEntity(httpServletRequest, queryExecutionId));
    } catch (TeilerControllerException e) {
      return createInternalServerError(e);
    }
  }

  private void generateFiles(TeilerCoreParameters teilerCoreParameters, Long queryExecutionId) {
    try {
      teilerCore.retrieveQuery(teilerCoreParameters).subscribe(path ->
          teilerDbService.saveQueryExecutionFile(createQueryExecutionFile(queryExecutionId,
              ((Path) path).toString())));
      teilerDbService.setQueryExecutionAsOk(queryExecutionId);
    } catch (TeilerCoreException e) {
      teilerDbService.setQueryExecutionAsError(queryExecutionId);
      teilerDbService.saveQueryExecutionErrorAndGetId(
          createQueryExecutionError(e, queryExecutionId));
      logger.error(ExceptionUtils.getStackTrace(e));
    }
  }

  private QueryExecutionError createQueryExecutionError(Exception e, Long queryExecutionId) {
    QueryExecutionError queryExecutionError = new QueryExecutionError();
    queryExecutionError.setQueryExecutionId(queryExecutionId);
    queryExecutionError.setError(ExceptionUtils.getStackTrace(e));
    return queryExecutionError;
  }

  private String createRequestResponseEntity(HttpServletRequest request, Long queryExecutionId)
      throws TeilerControllerException {
    try {
      return objectMapper.writeValueAsString(
          new RequestResponseEntity(fetchResponseUrl(request, queryExecutionId)));
    } catch (JsonProcessingException e) {
      throw new TeilerControllerException(e);
    }
  }

  private String fetchResponseUrl(HttpServletRequest httpServletRequest, Long queryExecutionId) {
    return ServletUriComponentsBuilder.fromRequestUri(httpServletRequest)
        .replacePath(TeilerConst.RESPONSE)
        .queryParam(TeilerConst.QUERY_EXECUTION_ID, queryExecutionId).toUriString();
  }

  private QueryExecution createQueryExecution(TeilerCoreParameters teilerCoreParameters) {
    QueryExecution queryExecution = new QueryExecution();
    queryExecution.setQueryId(teilerCoreParameters.query().getId());
    queryExecution.setExecutedAt(Instant.now());
    queryExecution.setStatus(Status.RUNNING);
    return queryExecution;
  }

  private QueryExecutionFile createQueryExecutionFile(Long queryExecutionId, String filePath) {
    QueryExecutionFile queryExecutionFile = new QueryExecutionFile();
    queryExecutionFile.setQueryExecutionId(queryExecutionId);
    queryExecutionFile.setFilePath(filePath);
    return queryExecutionFile;
  }

  @GetMapping(value = TeilerConst.RESPONSE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<InputStreamResource> getResponse(
      @RequestParam(name = TeilerConst.QUERY_EXECUTION_ID) Long queryExecutionId
  ) {
    Optional<QueryExecution> queryExecution = teilerDbService.fetchQueryExecution(queryExecutionId);
    if (queryExecution.isPresent()) {
      return switch (queryExecution.get().getStatus()) {
        case RUNNING -> ResponseEntity.accepted().build();
        case ERROR -> {
          List<QueryExecutionError> queryExecutionErrors = teilerDbService.fetchQueryExecutionErrorByQueryExecutionId(
              queryExecutionId);
          yield (queryExecutionErrors.size() > 0) ? ResponseEntity.internalServerError()
              .body(fetchErrorAsInputStreamResource(queryExecutionErrors.get(0)))
              : ResponseEntity.internalServerError().build();
        }
        case OK -> {
          try {
            yield fetchQueryExecutionFilesAndZipIfNecessary(queryExecutionId);
          } catch (TeilerControllerException | ZipperException | FileNotFoundException e) {
            yield createInternalServerError(e);
          }
        }
      };
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  private InputStreamResource fetchErrorAsInputStreamResource(
      QueryExecutionError queryExecutionError) {
    return new InputStreamResource(new ByteArrayInputStream(queryExecutionError.getError().getBytes(
        StandardCharsets.UTF_8)));
  }

  private ResponseEntity<InputStreamResource> fetchQueryExecutionFilesAndZipIfNecessary(
      Long queryExecutionFileId)
      throws TeilerControllerException, ZipperException, FileNotFoundException {
    List<QueryExecutionFile> queryExecutionFiles = teilerDbService.fetchQueryExecutionFilesByQueryExecutionId(
        queryExecutionFileId);
    if (queryExecutionFiles.size() > 0) {
      if (queryExecutionFiles.size() == 1) {
        return createResponseEntity(
            new InputStreamResource(new FileInputStream(queryExecutionFiles.get(0).getFilePath())),
            fetchFilename(queryExecutionFiles.get(0).getFilePath()));
      } else {
        Pair<InputStreamResource, String> inputStreamResourceFilenamePair = zipper.zipFiles(
            queryExecutionFiles.stream().map(queryExecutionFile -> queryExecutionFile.getFilePath())
                .toList());
        return createResponseEntity(inputStreamResourceFilenamePair.getFirst(),
            fetchFilename(inputStreamResourceFilenamePair.getSecond()));
      }
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  private String fetchFilename(String filePath) {
    int index = filePath.lastIndexOf(File.separator);
    return (index < 0) ? filePath : filePath.substring(index + 1);
  }

  private ResponseEntity<InputStreamResource> createResponseEntity(
      InputStreamResource inputStreamResource, String filename) {
    return ResponseEntity
        .ok()
        .header("Content-Disposition", "attachment; filename=" + filename)
        .body(inputStreamResource);
  }

  @GetMapping(value = TeilerConst.RETRIEVE_QUERY, produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<Path> retrieveQuery(
      @RequestParam(name = TeilerConst.QUERY_ID, required = false) Long queryId,
      @RequestParam(name = TeilerConst.QUERY, required = false) String query,
      @RequestParam(name = TeilerConst.SOURCE_ID) String sourceId,
      @RequestParam(name = TeilerConst.QUERY_FORMAT) Format queryFormat,
      @RequestParam(name = TeilerConst.OUTPUT_FORMAT) Format outputFormat,
      @RequestParam(name = TeilerConst.QUERY_LABEL, required = false) String queryLabel,
      @RequestParam(name = TeilerConst.QUERY_DESCRIPTION, required = false) String queryDescription,
      @RequestParam(name = TeilerConst.QUERY_CONTACT_ID, required = false) String queryContactId,
      @RequestParam(name = TeilerConst.QUERY_EXPIRATION_DATE, required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryExpirationDate,
      @RequestParam(name = TeilerConst.TEMPLATE_ID, required = false) String templateId,
      @RequestHeader(name = "Content-Type", required = false) String contentType,
      @RequestBody(required = false) String template
  ) {
    try {
      TeilerCoreParameters teilerCoreParameters = teilerCore.extractParameters(
          new TeilerParameters(queryId, query, sourceId, templateId, template, contentType,
              queryFormat, queryLabel, queryDescription, queryContactId, queryExpirationDate,
              outputFormat));
      return teilerCore.retrieveQuery(teilerCoreParameters);
    } catch (TeilerCoreException e) {
      //TODO
      throw new RuntimeException(e);
    }
  }


}
