package de.samply.exporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.samply.converter.Format;
import de.samply.core.ExporterCore;
import de.samply.core.ExporterCoreException;
import de.samply.core.ExporterCoreParameters;
import de.samply.core.ExporterParameters;
import de.samply.db.crud.ExporterDbService;
import de.samply.db.model.*;
import de.samply.explorer.*;
import de.samply.exporter.response.entity.CreateQueryResponseEntity;
import de.samply.exporter.response.entity.RequestResponseEntity;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.merger.FilesMergerManager;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateManager;
import de.samply.template.RequestTemplate;
import de.samply.template.graph.ConverterGraph;
import de.samply.template.graph.factory.ConverterTemplateGraphFactoryManager;
import de.samply.template.token.TokenContext;
import de.samply.utils.EnvironmentUtils;
import de.samply.utils.ProjectVersion;
import de.samply.zip.Zipper;
import de.samply.zip.ZipperException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@RestController
public class ExporterController {

    private final static Logger logger = BufferedLoggerFactory.getLogger(ExporterController.class);

    private ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    private final String projectVersion = ProjectVersion.getProjectVersion();
    private final ExporterCore exporterCore;
    private final ExplorerManager explorerManager;
    private final String httpRelativePath;
    private final String httpServletRequestScheme;
    private final ExporterDbService exporterDbService;
    private final ConverterTemplateManager converterTemplateManager;
    private final Zipper zipper;
    private final FilesMergerManager filesMergerManager;
    private final ConverterTemplateGraphFactoryManager converterTemplateGraphFactoryManager;
    private final EnvironmentUtils environmentUtils;

    public ExporterController(@Value(ExporterConst.HTTP_RELATIVE_PATH_SV) String httpRelativePath,
                              @Value(ExporterConst.HTTP_SERVLET_REQUEST_SCHEME_SV) String httpServletRequestScheme,
                              ExporterCore exporterCore,
                              ExporterDbService exporterDbService,
                              ConverterTemplateManager converterTemplateManager,
                              Zipper zipper,
                              ExplorerManager explorerManager,
                              FilesMergerManager filesMergerManager,
                              ConverterTemplateGraphFactoryManager converterTemplateGraphFactoryManager,
                              EnvironmentUtils environmentUtils) {
        this.httpRelativePath = httpRelativePath;
        this.httpServletRequestScheme = httpServletRequestScheme;
        this.exporterCore = exporterCore;
        this.exporterDbService = exporterDbService;
        this.zipper = zipper;
        this.explorerManager = explorerManager;
        this.converterTemplateManager = converterTemplateManager;
        this.filesMergerManager = filesMergerManager;
        this.converterTemplateGraphFactoryManager = converterTemplateGraphFactoryManager;
        this.environmentUtils = environmentUtils;
    }

    @CrossOrigin(origins = "${CROSS_ORIGINS}", allowedHeaders = {"Authorization"})
    @GetMapping(value = ExporterConst.INFO)
    @Operation(summary = "Get info", description = "Provides Information")
    public ResponseEntity<String> info() {
        return new ResponseEntity<>(projectVersion, HttpStatus.OK);
    }

    @PostMapping(value = ExporterConst.CREATE_QUERY, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Query", description = "Creates a new query based on the provided parameters.")
    public ResponseEntity<String> createQuery(@RequestParam(name = ExporterConst.QUERY, required = false) String query, // It can also be in requestTemplate
                                              @RequestParam(name = ExporterConst.QUERY_FORMAT) Format queryFormat,
                                              @RequestParam(name = ExporterConst.QUERY_LABEL) String queryLabel,
                                              @RequestParam(name = ExporterConst.QUERY_DESCRIPTION) String queryDescription,
                                              @RequestParam(name = ExporterConst.QUERY_CONTACT_ID) String queryContactId,
                                              @RequestParam(name = ExporterConst.QUERY_DEFAULT_TEMPLATE_ID, required = false) String defaultTemplateId,
                                              @RequestParam(name = ExporterConst.QUERY_CONTEXT, required = false) String queryContext,
                                              @RequestParam(name = ExporterConst.QUERY_DEFAULT_OUTPUT_FORMAT, required = false) Format defaultOutputFormat,
                                              @RequestParam(name = ExporterConst.QUERY_EXPIRATION_DATE, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryExpirationDate,
                                              @RequestHeader(name = "Content-Type", required = false) String contentType,
                                              @RequestBody(required = false) String requestTemplate) {
        Query tempQuery = new Query();
        String sQuery = null;
        if (query != null) {
            sQuery = query;
        } else if (requestTemplate != null) {
            if (contentType == null) {
                return ResponseEntity.badRequest().body("Content-Type not set");
            }
            try {
                RequestTemplate requestTemplate1 = exporterCore.fetchRequestTemplate(requestTemplate, contentType);
                sQuery = requestTemplate1.getQuery();
            } catch (IOException e) {
                return createInternalServerError(e);
            }
        }
        if (sQuery == null) {
            return ResponseEntity.badRequest().body("Query not set");
        }
        tempQuery.setQuery(sQuery);
        tempQuery.setFormat(queryFormat);
        tempQuery.setLabel(queryLabel);
        tempQuery.setDescription(queryDescription);
        tempQuery.setContactId(queryContactId);
        tempQuery.setExpirationDate(queryExpirationDate);
        tempQuery.setCreatedAt(Instant.now());
        tempQuery.setDefaultTemplateId(defaultTemplateId);
        tempQuery.setDefaultOutputFormat(defaultOutputFormat);
        tempQuery.setContext(queryContext);
        Long queryId = exporterDbService.saveQueryAndGetQueryId(tempQuery);

        try {
            return ResponseEntity.ok(createCreateQueryResponseEntity(queryId));
        } catch (ExporterControllerException e) {
            return createInternalServerError(e);
        }
    }

    private String createCreateQueryResponseEntity(Long queryId) throws ExporterControllerException {
        try {
            return objectMapper.writeValueAsString(new CreateQueryResponseEntity(queryId));
        } catch (JsonProcessingException e) {
            throw new ExporterControllerException(e);
        }
    }

    @GetMapping(value = ExporterConst.STATUS, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch Query Execution Status", description = "Retrieve the status of a query execution based on the provided ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of queries"),
            @ApiResponse(responseCode = "404", description = "Query not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> getQueryExecutionStatus(
            @Parameter(name = ExporterConst.QUERY_EXECUTION_ID, description = "ID der Abfrageausführung", required = true)
            @RequestParam(name = ExporterConst.QUERY_EXECUTION_ID) Long queryExecutionId) {
        return convertToResponseEntity(() -> exporterDbService.getQueryExecutionStatus(queryExecutionId).get());
    }

    @GetMapping(value = ExporterConst.FETCH_QUERIES, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch Queries", description = "Fetches queries based on the provided parameters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of queries"),
            @ApiResponse(responseCode = "404", description = "Query not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> fetchQueries(
            @RequestParam(name = ExporterConst.PAGE, required = false) Integer page,
            @RequestParam(name = ExporterConst.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = ExporterConst.QUERY_ID, required = false) Long queryId) {
        return (queryId != null) ?
                convertToResponseEntity(() -> exporterDbService.fetchQuery(queryId).get()) :
                convertToResponseEntity(page, pageSize, exporterDbService::fetchAllQueries, exporterDbService::fetchAllQueries);
    }


    @GetMapping(value = ExporterConst.FETCH_QUERY_EXECUTION_ERRORS, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch Query Execution Errors", description = "Fetches query execution errors based on the provided parameters.")
    /*@ApiImplicitParams({
            @ApiImplicitParam(name = ExporterConst.PAGE, value = "Page number for pagination", required = false, dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = ExporterConst.PAGE_SIZE, value = "Number of items per page", required = false, dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = ExporterConst.QUERY_EXECUTION_ID, value = "ID of the query execution", required = false, dataType = "Long", paramType = "query")
    })*/
    public ResponseEntity<String> fetchQueryExecutionErrors(
            @RequestParam(name = ExporterConst.PAGE, required = false) Integer page,
            @RequestParam(name = ExporterConst.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = ExporterConst.QUERY_EXECUTION_ID, required = false) Long queryExecutionId) {
        return (queryExecutionId != null) ?
                convertToResponseEntity(page, pageSize, () -> exporterDbService.fetchQueryExecutionErrorByQueryExecutionId(queryExecutionId), (p, s) -> exporterDbService.fetchQueryExecutionErrorByQueryExecutionId(queryExecutionId, p, s)) :
                convertToResponseEntity(page, pageSize, exporterDbService::fetchAllQueryExecutionErrors, exporterDbService::fetchAllQueryExecutionErrors);
    }

    @GetMapping(value = ExporterConst.INQUIRY, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch Inquiry", description = "Fetches an inquiry based on the provided query ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully fetched the inquiry"),
            @ApiResponse(responseCode = "404", description = "Inquiry not found")
    })
    public ResponseEntity<String> fetchInquiry(
            HttpServletRequest httpServletRequest,
            @RequestHeader(name = ExporterConst.IS_INTERNAL_REQUEST, required = false) Boolean isInternalRequest,
            @RequestParam(name = ExporterConst.QUERY_ID) Long queryId) {
        try {
            Optional<Inquiry> inquiryOptional = exporterDbService.fetchInquiry(queryId);
            if (inquiryOptional.isPresent()) {
                addExecutionFileUrl(httpServletRequest, inquiryOptional.get(), isInternalRequest);
                return ResponseEntity.ok().body(objectMapper.writeValueAsString(inquiryOptional.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (JsonProcessingException e) {
            return createInternalServerError(e);
        }
    }

    private void addExecutionFileUrl(HttpServletRequest request, Inquiry inquiry, Boolean isInternalRequest) {
        if (inquiry.getQueryExecutionId() != null) {
            inquiry.setExecutionFileUrl(fetchResponseUrl(request, inquiry.getQueryExecutionId(), isInternalRequest));
        }
    }


    @PutMapping(value = ExporterConst.ARCHIVE_QUERY, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Archive Query", description = "Archives a query based on the provided query ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully archived the query"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> archiveQuery(
            @Parameter(name = ExporterConst.QUERY_ID, description = "ID of the query to be archived", required = true)
            @RequestParam(name = ExporterConst.QUERY_ID) Long queryId) {
        try {
            exporterDbService.archiveQuery(queryId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return createInternalServerError(e);
        }
    }


    @GetMapping(value = ExporterConst.ACTIVE_INQUIRIES, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch Active Inquiries", description = "Fetches active inquiries based on the provided parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully fetched active inquiries"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> fetchActiveInquiries(
            @Parameter(name = ExporterConst.PAGE, description = "Page number for pagination", required = false)
            @RequestParam(name = ExporterConst.PAGE, required = false) Integer page,
            @Parameter(name = ExporterConst.PAGE_SIZE, description = "Number of items per page", required = false)
            @RequestParam(name = ExporterConst.PAGE_SIZE, required = false) Integer pageSize) {
        return convertToResponseEntity(page, pageSize, exporterDbService::fetchActiveInquiries,
                exporterDbService::fetchActiveInquiries);
    }


    @GetMapping(value = ExporterConst.ARCHIVED_INQUIRIES, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch Archived Inquiries", description = "Fetches archived inquiries based on the provided parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully fetched archived inquiries"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> fetchArchivedInquiries(
            @Parameter(name = ExporterConst.PAGE, description = "Page number for pagination", required = false)
            @RequestParam(name = ExporterConst.PAGE, required = false) Integer page,
            @Parameter(name = ExporterConst.PAGE_SIZE, description = "Number of items per page", required = false)
            @RequestParam(name = ExporterConst.PAGE_SIZE, required = false) Integer pageSize) {
        // Method implementation
        return convertToResponseEntity(page, pageSize, exporterDbService::fetchArchivedInquiries,
                exporterDbService::fetchArchivedInquiries);
    }

    @GetMapping(value = ExporterConst.ERROR_INQUIRIES, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch Error Inquiries", description = "Fetches error inquiries based on the provided parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully fetched error inquiries"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> fetchErrorInquiries(
            @Parameter(name = ExporterConst.PAGE, description = "Page number for pagination", required = false)
            @RequestParam(name = ExporterConst.PAGE, required = false) Integer page,
            @Parameter(name = ExporterConst.PAGE_SIZE, description = "Number of items per page", required = false)
            @RequestParam(name = ExporterConst.PAGE_SIZE, required = false) Integer pageSize) {
        return convertToResponseEntity(page, pageSize, exporterDbService::fetchErrorInquiries,
                exporterDbService::fetchErrorInquiries);
    }


    private ResponseEntity createInternalServerError(Exception e) {
        return ResponseEntity.internalServerError().body(ExceptionUtils.getStackTrace(e));
    }

    private <T> ResponseEntity convertToResponseEntity(Integer page, Integer pageSize,
                                                       Supplier<T> supplier, BiFunction<Integer, Integer, T> pagePageSizeBifunction) {
        if (page == null && pageSize == null) {
            return convertToResponseEntity(supplier);
        } else if (page != null && pageSize != null) {
            return convertToResponseEntity(page, pageSize, pagePageSizeBifunction);
        } else {
            return ResponseEntity.badRequest()
                    .body((page == null) ? "Page not provided" : "Page size not provided");
        }
    }

    private <T, R> ResponseEntity convertToResponseEntity(T input, Function<T, R> function) {
        return convertToResponseEntity(() -> function.apply(input));
    }

    private <T> ResponseEntity convertToResponseEntity(Supplier<T> supplier) {
        try {
            T result = supplier.get();
            return (result != null) ? ResponseEntity.ok(objectMapper.writeValueAsString(result))
                    : ResponseEntity.notFound().build();
        } catch (JsonProcessingException e) {
            return createInternalServerError(e);
        }
    }

    private <T> ResponseEntity convertToResponseEntity(int page, int pageSize,
                                                       BiFunction<Integer, Integer, T> pagePageSizeBifunction) {
        try {
            return ResponseEntity.ok(
                    objectMapper.writeValueAsString(pagePageSizeBifunction.apply(page, pageSize)));
        } catch (JsonProcessingException e) {
            return createInternalServerError(e);
        }
    }

    @PostMapping(value = ExporterConst.REQUEST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Post Request", description = "Processes and handles the export request based on the provided parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully processed export request"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> postRequest(HttpServletRequest httpServletRequest,
                                              @RequestParam(name = ExporterConst.QUERY_ID, required = false) Long queryId,
                                              @RequestParam(name = ExporterConst.QUERY, required = false) String query,
                                              @RequestParam(name = ExporterConst.QUERY_FORMAT, required = false) Format queryFormat,
                                              @RequestParam(name = ExporterConst.QUERY_LABEL, required = false) String queryLabel,
                                              @RequestParam(name = ExporterConst.QUERY_DESCRIPTION, required = false) String queryDescription,
                                              @RequestParam(name = ExporterConst.QUERY_CONTEXT, required = false) String queryContext,
                                              @RequestParam(name = ExporterConst.QUERY_CONTACT_ID, required = false) String queryContactId,
                                              @RequestParam(name = ExporterConst.QUERY_EXPIRATION_DATE, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryExpirationDate,
                                              @RequestParam(name = ExporterConst.OUTPUT_FORMAT) Format outputFormat,
                                              @RequestParam(name = ExporterConst.TEMPLATE_ID, required = false) String templateId,
                                              @RequestParam(name = ExporterConst.QUERY_EXECUTION_CONTACT_ID, required = false) String queryExecutionContactId,
                                              @RequestHeader(name = "Content-Type", required = false) String contentType,
                                              @RequestHeader(name = ExporterConst.IS_INTERNAL_REQUEST, required = false) Boolean isInternalRequest,
                                              @RequestBody(required = false) String requestTemplate) {
        ExporterCoreParameters exporterCoreParameters;
        Map<String, String> requestParameters = fetchParameterMap(httpServletRequest);
        try {
            exporterCoreParameters = exporterCore.extractParameters(
                    new ExporterParameters(queryId, query, templateId, requestTemplate, contentType, queryFormat,
                            queryLabel, queryDescription, queryContactId, queryContext, queryExecutionContactId, queryExpirationDate, outputFormat));
        } catch (ExporterCoreException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        Long queryExecutionId = exporterDbService.saveQueryExecutionAndGetExecutionId(
                createQueryExecution(exporterCoreParameters));
        final ExporterCoreParameters tempExporterCoreParameters = exporterCoreParameters;
        new Thread(() -> generateFiles(tempExporterCoreParameters, queryExecutionId, createNewTokenContext(requestParameters, queryExecutionId))).start();
        try {
            return ResponseEntity.ok()
                    .body(createRequestResponseEntity(httpServletRequest, queryExecutionId, isInternalRequest));
        } catch (ExporterControllerException e) {
            return createInternalServerError(e);
        }
    }

    private TokenContext createNewTokenContext(Map<String, String> requestParameters, Long queryExecutionId) {
        return createNewTokenContext(requestParameters, exporterDbService.fetchQueryByQueryExecutionId(queryExecutionId).get());
    }

    private TokenContext createNewTokenContext(Map<String, String> requestParameters, Query query) {
        TokenContext tokenContext = new TokenContext(environmentUtils);
        tokenContext.addKeyValues(requestParameters);
        tokenContext.addKeyValues(query);
        return tokenContext;
    }

    private Map<String, String> fetchParameterMap(HttpServletRequest httpServletRequest) {
        Map<String, String> result = new HashMap<>();
        if (httpServletRequest != null) {
            httpServletRequest.getParameterMap().keySet().forEach(key -> {
                String[] values = httpServletRequest.getParameterMap().get(key);
                if (values != null && values.length >= 1) {
                    result.put(key, values[0]);
                }
            });
        }
        return result;
    }


    private void generateFiles(ExporterCoreParameters exporterCoreParameters, Long queryExecutionId, TokenContext tokenContext) {
        try {
            exporterCore.retrieveQuery(exporterCoreParameters, tokenContext).subscribe(
                    path -> exporterDbService.saveQueryExecutionFile(
                            createQueryExecutionFile(queryExecutionId, ((Path) path).toString())));
            exporterDbService.setQueryExecutionAsOk(queryExecutionId);
            BufferedLoggerFactory.clearBuffer();
        } catch (ExporterCoreException e) {
            exporterDbService.setQueryExecutionAsError(queryExecutionId);
            exporterDbService.saveQueryExecutionErrorAndGetId(
                    createQueryExecutionError(e, queryExecutionId));
            BufferedLoggerFactory.clearBuffer();
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private QueryExecutionError createQueryExecutionError(Exception e, Long queryExecutionId) {
        QueryExecutionError queryExecutionError = new QueryExecutionError();
        queryExecutionError.setQueryExecutionId(queryExecutionId);
        queryExecutionError.setError(ExceptionUtils.getStackTrace(e));
        return queryExecutionError;
    }

    private String createRequestResponseEntity(HttpServletRequest request, Long queryExecutionId, Boolean isInternalRequest)
            throws ExporterControllerException {
        try {
            return objectMapper.writeValueAsString(
                    new RequestResponseEntity(fetchResponseUrl(request, queryExecutionId, isInternalRequest)));
        } catch (JsonProcessingException e) {
            throw new ExporterControllerException(e);
        }
    }

    private String fetchResponseUrl(HttpServletRequest httpServletRequest, Long queryExecutionId, Boolean isInternalRequest) {
        ServletUriComponentsBuilder servletUriComponentsBuilder = ServletUriComponentsBuilder.fromRequestUri(
                httpServletRequest);
        if (isInternalRequest != null && isInternalRequest) {
            servletUriComponentsBuilder
                    .scheme("http")
                    .replacePath(ExporterConst.RESPONSE);
        } else {
            servletUriComponentsBuilder
                    .scheme(httpServletRequestScheme)
                    .replacePath(createHttpPath(ExporterConst.RESPONSE));
        }
        String result = servletUriComponentsBuilder
                .queryParam(ExporterConst.QUERY_EXECUTION_ID, queryExecutionId).toUriString();
        logger.info("Response URL: " + result);
        return result;
    }

    private String createHttpPath(String httpPath) {
        return (httpRelativePath != null && httpRelativePath.length() > 0) ? httpRelativePath + '/'
                + httpPath : httpPath;
    }

    private QueryExecution createQueryExecution(ExporterCoreParameters exporterCoreParameters) {
        QueryExecution queryExecution = new QueryExecution();
        queryExecution.setQueryId(exporterCoreParameters.query().getId());
        queryExecution.setExecutedAt(Instant.now());
        queryExecution.setStatus(Status.RUNNING);
        queryExecution.setOutputFormat(exporterCoreParameters.converter().getOutputFormat());
        queryExecution.setTemplateId(exporterCoreParameters.template().getId());
        queryExecution.setContactId(exporterCoreParameters.queryExecutionContactId());
        return queryExecution;
    }

    private QueryExecutionFile createQueryExecutionFile(Long queryExecutionId, String filePath) {
        QueryExecutionFile queryExecutionFile = new QueryExecutionFile();
        queryExecutionFile.setQueryExecutionId(queryExecutionId);
        queryExecutionFile.setFilePath(filePath);
        return queryExecutionFile;
    }

    @CrossOrigin(origins = "${CROSS_ORIGINS}", allowedHeaders = {"Authorization"}, exposedHeaders = {
            "Content-Disposition"})
    @GetMapping(value = ExporterConst.RESPONSE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Get Response", description = "Gets the export response based on the provided parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Accepted - Query execution in progress"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "200", description = "OK - Successfully processed export request"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "404", description = "Not Found - Query execution not found")
    })
    public ResponseEntity<InputStreamResource> getResponse(
            @RequestParam(name = ExporterConst.QUERY_EXECUTION_ID) Long queryExecutionId,
            @RequestParam(name = ExporterConst.FILE_FILTER, required = false) String fileFilter,
            @RequestParam(name = ExporterConst.FILE_COLUMN_PIVOT, required = false) String fileColumnPivot,
            @RequestParam(name = ExporterConst.PAGE, required = false) Integer pageCounter,
            @RequestParam(name = ExporterConst.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = ExporterConst.PIVOT_VALUE, required = false) String pivotValue,
            @RequestParam(name = ExporterConst.MERGE_FILES, required = false) Boolean mergeFiles,
            @RequestParam(name = ExporterConst.FILE_AS_PLAIN_TEXT_IN_BODY, required = false) Boolean fileAsPlainTextInBody,
            HttpServletRequest httpServletRequest) {
        Map<String, String> requestParameters = fetchParameterMap(httpServletRequest);
        Optional<QueryExecution> queryExecution = exporterDbService.fetchQueryExecution(
                queryExecutionId);
        if (queryExecution.isPresent()) {
            return switch (queryExecution.get().getStatus()) {
                case RUNNING -> ResponseEntity.accepted().build();
                case ERROR -> {
                    List<QueryExecutionError> queryExecutionErrors = exporterDbService.fetchQueryExecutionErrorByQueryExecutionId(
                            queryExecutionId);
                    yield (queryExecutionErrors.size() > 0) ? ResponseEntity.internalServerError()
                            .body(fetchErrorAsInputStreamResource(queryExecutionErrors.get(0)))
                            : ResponseEntity.internalServerError().build();
                }
                case OK -> {
                    try {
                        yield fetchQueryExecutionFilesAndZipOrMergeIfNecessary(
                                queryExecutionId, fileFilter, fileColumnPivot, pageCounter, pivotValue, pageSize, mergeFiles, fileAsPlainTextInBody, createNewTokenContext(requestParameters, queryExecutionId));
                    } catch (ExporterControllerException | ZipperException | ExplorerException |
                             PivotIdentifierException | IOException e) {
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
        return new InputStreamResource(
                new ByteArrayInputStream(queryExecutionError.getError().getBytes(StandardCharsets.UTF_8)));
    }

    private ResponseEntity<InputStreamResource> fetchQueryExecutionFilesAndZipOrMergeIfNecessary(
            Long queryExecutionFileId, String fileFilter, String fileColumnPivot, Integer pageCounter,
            String pivotValue, Integer pageSize, Boolean mergeFiles, Boolean fileAsPlainTextInBody, TokenContext tokenContext)
            throws ExporterControllerException, ZipperException, IOException, ExplorerException, PivotIdentifierException {
        List<QueryExecutionFile> queryExecutionFiles = exporterDbService.fetchQueryExecutionFilesByQueryExecutionId(
                queryExecutionFileId);
        List<Path> files = convertToPath(queryExecutionFiles);
        HttpHeaders httpHeaders = new HttpHeaders();
        if (fileColumnPivot != null && (pageCounter != null || pivotValue != null)) {
            FilesAndNumberOfPagesOfPivot filesAndNumberOfPagesOfPivot = filterFiles(files, fileColumnPivot, pageCounter, pivotValue, pageSize, tokenContext);
            files = filesAndNumberOfPagesOfPivot.paths();
            httpHeaders.add(ExporterConst.NUMBER_OF_PAGES, String.valueOf(filesAndNumberOfPagesOfPivot.numberOfPagesOfPivot()));
        }
        files = filterFiles(files, fileFilter);
        if (files.size() > 0) {
            if (files.size() == 1) {
                return createResponseEntity(files.get(0), httpHeaders, fileAsPlainTextInBody);
            } else if (mergeFiles != null && mergeFiles == true && filesMergerManager.isFileExtensionSupported(files)) {
                Optional<Path> mergedFile = filesMergerManager.merge(files, tokenContext);
                return (mergedFile.isPresent()) ? createResponseEntity(mergedFile.get(), httpHeaders, fileAsPlainTextInBody) : ResponseEntity.notFound().build();
            } else {
                Pair<InputStreamResource, String> inputStreamResourceFilenamePair = zipper.zipFiles(files, tokenContext);
                return createResponseEntity(inputStreamResourceFilenamePair.getFirst(),
                        fetchFilename(inputStreamResourceFilenamePair.getSecond()), httpHeaders);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<InputStreamResource> createResponseEntity(Path path, HttpHeaders httpHeaders, Boolean fileAsPlainTextInBody) throws FileNotFoundException {
        InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(path.toAbsolutePath().toString()));
        return (fileAsPlainTextInBody != null && fileAsPlainTextInBody) ?
                createResponseEntityAsPlainTextInBody(inputStreamResource, httpHeaders) :
                createResponseEntity(inputStreamResource, fetchFilename(path.getFileName().toString()), httpHeaders);
    }

    private List<Path> convertToPath(List<QueryExecutionFile> queryExecutionFiles) {
        return queryExecutionFiles.stream().map(queryExecutionFile -> Path.of(queryExecutionFile.getFilePath())).toList();
    }

    private FilesAndNumberOfPagesOfPivot filterFiles(List<Path> files, String fileColumnPivot, Integer pageCounter, String pivotValue, Integer pageSize, TokenContext tokenContext) throws PivotIdentifierException, ExplorerException {
        List<Path> result = new ArrayList<>();
        PivotIdentifier pivotIdentifier = new PivotIdentifier(fileColumnPivot);
        Path pivotFile = null;
        for (Path file : files) {
            if (file.getFileName().toString().contains(pivotIdentifier.getFilename())) {
                pivotFile = file;
                break;
            }
        }
        if (pivotFile == null) {
            throw new PivotIdentifierException("File not found");
        }
        Optional<Explorer> explorer = explorerManager.getExplorer(pivotFile);
        if (explorer.isEmpty()) {
            throw new PivotIdentifierException("Source file not found");
        }
        Pivot[] pivots;
        if (pageCounter != null) {
            if (pageSize == null) {
                pageSize = 1;
            }
            Optional<Pivot[]> tempPivots = explorer.get().fetchPivot(pivotFile, pivotIdentifier.getColumn(), pageCounter, pageSize);
            if (tempPivots.isEmpty()) {
                throw new PivotIdentifierException("Counter out of range");
            }
            pivots = tempPivots.get();
        } else {
            pivots = new Pivot[]{new Pivot(pivotIdentifier.getColumn(), pivotValue)};
        }
        try {
            files.forEach(file -> {
                try {
                    result.add(explorer.get().filter(file, pivots, tokenContext));
                } catch (ExplorerException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new PivotIdentifierException(e);
        }
        return new FilesAndNumberOfPagesOfPivot(result, fetchNumberOfPagesOfPivot(pivotFile, explorer.get(), pageSize));
    }

    private int fetchNumberOfPagesOfPivot(Path pivotFile, Explorer explorer, int pageSize) throws ExplorerException {
        int totalNumberOfElements = explorer.fetchTotalNumberOfElements(pivotFile);
        return Double.valueOf(Math.ceil(1.0 * totalNumberOfElements / pageSize)).intValue();
    }

    private List<Path> filterFiles(List<Path> files, String fileFilter) {
        List<Path> result;
        if (fileFilter != null) {
            result = new ArrayList<>();
            String[] filters = fileFilter.trim().split(ExporterConst.FILE_FILTER_SEPARATOR);
            files.forEach(file -> {
                for (String filter : filters) {
                    if (file.toAbsolutePath().toString().toLowerCase().contains(filter.toLowerCase())) {
                        result.add(file);
                        break;
                    }
                }
            });
        } else {
            result = files;
        }
        return result;
    }

    private String fetchFilename(String filePath) {
        int index = filePath.lastIndexOf(File.separator);
        return (index < 0) ? filePath : filePath.substring(index + 1);
    }

    private ResponseEntity<InputStreamResource> createResponseEntity(
            InputStreamResource inputStreamResource, String filename) {
        return createResponseEntity(inputStreamResource, filename, new HttpHeaders());
    }

    private ResponseEntity<InputStreamResource> createResponseEntity(
            InputStreamResource inputStreamResource, String filename, HttpHeaders httpHeaders) {
        return ResponseEntity.ok().headers(httpHeaders).header("Content-Disposition", "attachment; filename=" + filename)
                .body(inputStreamResource);
    }

    private ResponseEntity<InputStreamResource> createResponseEntityAsPlainTextInBody(
            InputStreamResource inputStreamResource, HttpHeaders httpHeaders) {
        return ResponseEntity.ok().headers(httpHeaders).contentType(MediaType.TEXT_PLAIN).body(inputStreamResource);
    }

    @GetMapping(value = ExporterConst.LOGS)
    @Operation(summary = "Fetch Logs", description = "Fetches log entries based on the provided parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK - Successfully fetched logs")
    })
    public ResponseEntity<String[]> fetchLogs(
            @Parameter(name = ExporterConst.LOGS_SIZE, description = "Number of log entries to fetch", required = true)
            @RequestParam(name = ExporterConst.LOGS_SIZE) int logsSize,
            @Parameter(name = ExporterConst.LOGS_LAST_LINE, description = "Last log entry identifier (optional)", required = false)
            @RequestParam(name = ExporterConst.LOGS_LAST_LINE, required = false) String logsLastLine) {
        return ResponseEntity.ok().body(BufferedLoggerFactory.getLastLoggerLines(logsSize, logsLastLine));
    }

    @CrossOrigin(origins = "${CROSS_ORIGINS}", allowedHeaders = {"Authorization"})
    @GetMapping(value = ExporterConst.TEMPLATE_IDS)
    @Operation(summary = "Fetch Template IDs", description = "Fetches available template IDs.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK - Successfully fetched the template"),
    })
    public ResponseEntity<String[]> fetchTemplateIds() {
        return ResponseEntity.ok().body(converterTemplateManager.getConverterTemplateIds().toArray(new String[0]));
    }

    @CrossOrigin(origins = "${CROSS_ORIGINS}", allowedHeaders = {"Authorization"})
    @GetMapping(value = ExporterConst.TEMPLATE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Fetch Template", description = "Fetches a template based on the provided template ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK - Successfully fetched the template"),
            @ApiResponse(responseCode = "404", description = "Not Found - Template not found")
    })
    public ResponseEntity<InputStreamResource> fetchTemplate(
            @Parameter(name = ExporterConst.TEMPLATE_ID, description = "ID of the template to be fetched", required = true)
            @RequestParam(name = ExporterConst.TEMPLATE_ID) String templateId
    ) throws FileNotFoundException {
        Optional<Path> templatePath = converterTemplateManager.getTemplatePath(templateId);
        if (templatePath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return createResponseEntity(templatePath.get());
    }

    private ResponseEntity<InputStreamResource> createResponseEntity(Path path)
            throws FileNotFoundException {
        return createResponseEntity(new InputStreamResource(new FileInputStream(path.toFile())),
                path.getFileName().toString());
    }

    @CrossOrigin(origins = "${CROSS_ORIGINS}", allowedHeaders = {"Authorization"})
    @GetMapping(value = ExporterConst.INPUT_FORMATS)
    @Operation(summary = "Fetch Input Formats", description = "Fetches available input formats.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK - Successfully fetched input formats")
    })
    public ResponseEntity<String[]> fetchInputFormats() {
        return ResponseEntity.ok().body(Format.fetchQueries());
    }

    @CrossOrigin(origins = "${CROSS_ORIGINS}", allowedHeaders = {"Authorization"})
    @GetMapping(value = ExporterConst.OUTPUT_FORMATS)
    @Operation(summary = "Fetch Output Formats", description = "Fetches available output formats.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK - Successfully fetched output formats")
    })
    public ResponseEntity<String[]> fetchOutputFormats() {
        return ResponseEntity.ok().body(Format.fetchOutputs());
    }

    @CrossOrigin(origins = "${CROSS_ORIGINS}", allowedHeaders = {"Authorization"})
    @GetMapping(value = ExporterConst.TEMPLATE_GRAPH)
    @Operation(summary = "Fetch Template Graph", description = "Fetches the template graph based on the provided parameters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK - Successfully fetched the template graph"),
            @ApiResponse(responseCode = "400", description = "Bad Request - template-id or query-execution-id are missing"),
            @ApiResponse(responseCode = "404", description = "Not Found - Template or query execution not found")
    })
    public ResponseEntity<String> fetchOutputFormats(
            @Parameter(name = ExporterConst.TEMPLATE_ID, description = "ID of the template to fetch the graph for", required = false)
            @RequestParam(name = ExporterConst.TEMPLATE_ID, required = false) String templateId,
            @Parameter(name = ExporterConst.QUERY_EXECUTION_ID, description = "ID of the query execution to fetch the template graph for", required = false)
            @RequestParam(name = ExporterConst.QUERY_EXECUTION_ID, required = false) Long queryExecutionId,
            @Parameter(name = ExporterConst.OUTPUT_FORMAT, description = "Output format for the template graph", required = false)
            @RequestParam(name = ExporterConst.OUTPUT_FORMAT, required = false, defaultValue = ExporterConst.DEFAULT_GRAPH_FORMAT) Format outputFormat
    ) {
        if (queryExecutionId != null) {
            Optional<QueryExecution> queryExecution = exporterDbService.fetchQueryExecution(queryExecutionId);
            if (queryExecution.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            templateId = queryExecution.get().getTemplateId();
        }
        if (templateId != null) {
            ConverterTemplate converterTemplate = converterTemplateManager.getConverterTemplate(templateId);
            Optional<ConverterGraph> converterTemplateGraph = converterTemplateGraphFactoryManager.fetchConverterTemplateGraph(converterTemplate, outputFormat);
            return convertToResponseEntity(converterTemplateGraph::get);
        }
        return ResponseEntity.badRequest().body("template-id or query-execution-id are missing");
    }

    @GetMapping(value = ExporterConst.RUNNING_QUERIES)
    @Operation(summary = "Fetch Running Exports", description = "Fetches information about running query executions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK - Successfully fetched running exports")
    })
    public ResponseEntity<String> fetchRunningExports() {
        return convertToResponseEntity(exporterDbService::fetchRunningQueryExecutions);
    }

    @PostMapping(value = ExporterConst.UPDATE_QUERY, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update Query", description = "Updates an existing query based on the provided parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK - Successfully updated the query"),
            @ApiResponse(responseCode = "404", description = "Not Found - Query not found")
    })
    public ResponseEntity<String> updateQuery(
            @Parameter(name = ExporterConst.QUERY_ID, description = "ID of the query to be updated", required = true)
            @RequestParam(name = ExporterConst.QUERY_ID) Long queryId,
            @Parameter(name = ExporterConst.QUERY_LABEL, description = "New label for the query", required = false)
            @RequestParam(name = ExporterConst.QUERY_LABEL, required = false) String queryLabel,
            @Parameter(name = ExporterConst.QUERY_DESCRIPTION, description = "New description for the query", required = false)
            @RequestParam(name = ExporterConst.QUERY_DESCRIPTION, required = false) String queryDescription,
            @Parameter(name = ExporterConst.QUERY_DEFAULT_TEMPLATE_ID, description = "New default template ID for the query", required = false)
            @RequestParam(name = ExporterConst.QUERY_DEFAULT_TEMPLATE_ID, required = false) String defaultTemplateId,
            @Parameter(name = ExporterConst.QUERY_DEFAULT_OUTPUT_FORMAT, description = "New default output format for the query", required = false)
            @RequestParam(name = ExporterConst.QUERY_DEFAULT_OUTPUT_FORMAT, required = false) Format defaultOutputFormat,
            @Parameter(name = ExporterConst.QUERY_EXPIRATION_DATE, description = "New expiration date for the query", required = false)
            @RequestParam(name = ExporterConst.QUERY_EXPIRATION_DATE, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryExpirationDate) {
        Optional<Query> optionalQuery = exporterDbService.fetchQuery(queryId);
        if (optionalQuery.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Query query = optionalQuery.get();
        Map<Object, Runnable> updaters = new HashMap<>();
        updaters.put(queryLabel, () -> query.setLabel(queryLabel));
        updaters.put(queryDescription, () -> query.setDescription(queryDescription));
        updaters.put(defaultTemplateId, () -> query.setDefaultTemplateId(defaultTemplateId));
        updaters.put(defaultOutputFormat, () -> query.setDefaultOutputFormat(defaultOutputFormat));
        updaters.put(queryExpirationDate, () -> query.setExpirationDate(queryExpirationDate));
        updaters.keySet().forEach(key -> {
            if (key != null) {
                updaters.get(key).run();
            }
        });
        exporterDbService.saveQueryAndGetQueryId(query);
        return ResponseEntity.ok().build();
    }


}
