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
import de.samply.template.ConverterTemplateManager;
import de.samply.utils.ProjectVersion;
import de.samply.zip.Zipper;
import de.samply.zip.ZipperException;
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
import reactor.core.publisher.Flux;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@RestController
public class ExporterController {

    private final static Logger logger = BufferedLoggerFactory.getLogger(ExporterController.class);

    private ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule());
    private final String projectVersion = ProjectVersion.getProjectVersion();
    private final ExporterCore exporterCore;
    private final ExplorerManager explorerManager;
    private final String httpRelativePath;
    private final String httpServletRequestScheme;
    private final ExporterDbService exporterDbService;
    private final ConverterTemplateManager converterTemplateManager;
    private final Zipper zipper;
    private final FilesMergerManager filesMergerManager;

    public ExporterController(@Value(ExporterConst.HTTP_RELATIVE_PATH_SV) String httpRelativePath,
                              @Value(ExporterConst.HTTP_SERVLET_REQUEST_SCHEME_SV) String httpServletRequestScheme,
                              ExporterCore exporterCore,
                              ExporterDbService exporterDbService,
                              ConverterTemplateManager converterTemplateManager,
                              Zipper zipper,
                              ExplorerManager explorerManager,
                              FilesMergerManager filesMergerManager) {
        this.httpRelativePath = httpRelativePath;
        this.httpServletRequestScheme = httpServletRequestScheme;
        this.exporterCore = exporterCore;
        this.exporterDbService = exporterDbService;
        this.zipper = zipper;
        this.explorerManager = explorerManager;
        this.converterTemplateManager = converterTemplateManager;
        this.filesMergerManager = filesMergerManager;
    }

    @CrossOrigin(origins = "${CROSS_ORIGINS}", allowedHeaders = {"Authorization"})
    @GetMapping(value = ExporterConst.INFO)
    public ResponseEntity<String> info() {
        return new ResponseEntity<>(projectVersion, HttpStatus.OK);
    }

    @PostMapping(value = ExporterConst.CREATE_QUERY, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createQuery(@RequestParam(name = ExporterConst.QUERY) String query,
                                              @RequestParam(name = ExporterConst.QUERY_FORMAT) Format queryFormat,
                                              @RequestParam(name = ExporterConst.QUERY_LABEL) String queryLabel,
                                              @RequestParam(name = ExporterConst.QUERY_DESCRIPTION) String queryDescription,
                                              @RequestParam(name = ExporterConst.QUERY_CONTACT_ID) String queryContactId,
                                              @RequestParam(name = ExporterConst.QUERY_EXPIRATION_DATE, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryExpirationDate) {
        Query tempQuery = new Query();
        tempQuery.setQuery(query);
        tempQuery.setFormat(queryFormat);
        tempQuery.setLabel(queryLabel);
        tempQuery.setDescription(queryDescription);
        tempQuery.setContactId(queryContactId);
        tempQuery.setExpirationDate(queryExpirationDate);
        tempQuery.setCreatedAt(Instant.now());
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
    public ResponseEntity<String> getQueryExecutionStatus(
            @RequestParam(name = ExporterConst.QUERY_EXECUTION_ID) Long queryExecutionId) {
        return convertToResponseEntity(queryExecutionId, exporterDbService::getQueryExecutionStatus);
    }

    @GetMapping(value = ExporterConst.QUERIES, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetchQueries(
            @RequestParam(name = ExporterConst.PAGE, required = false) Integer page,
            @RequestParam(name = ExporterConst.PAGE_SIZE, required = false) Integer pageSize) {
        return convertToResponseEntity(page, pageSize, exporterDbService::fetchAllQueries,
                exporterDbService::fetchAllQueries);
    }

    @GetMapping(value = ExporterConst.INQUIRY, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetchInquiry(HttpServletRequest httpServletRequest,
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
    public ResponseEntity<String> archiveQuery(
            @RequestParam(name = ExporterConst.QUERY_ID) Long queryId) {
        try {
            exporterDbService.archiveQuery(queryId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return createInternalServerError(e);
        }
    }

    @GetMapping(value = ExporterConst.ACTIVE_INQUIRIES, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetchActiveInquiries(
            @RequestParam(name = ExporterConst.PAGE, required = false) Integer page,
            @RequestParam(name = ExporterConst.PAGE_SIZE, required = false) Integer pageSize) {
        return convertToResponseEntity(page, pageSize, exporterDbService::fetchActiveInquiries,
                exporterDbService::fetchActiveInquiries);
    }

    @GetMapping(value = ExporterConst.ARCHIVED_INQUIRIES, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetchArchivedInquiries(
            @RequestParam(name = ExporterConst.PAGE, required = false) Integer page,
            @RequestParam(name = ExporterConst.PAGE_SIZE, required = false) Integer pageSize) {
        return convertToResponseEntity(page, pageSize, exporterDbService::fetchArchivedInquiries,
                exporterDbService::fetchArchivedInquiries);
    }

    @GetMapping(value = ExporterConst.ERROR_INQUIRIES, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetchErrorInquiries(
            @RequestParam(name = ExporterConst.PAGE, required = false) Integer page,
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
    public ResponseEntity<String> postRequest(HttpServletRequest httpServletRequest,
                                              @RequestParam(name = ExporterConst.QUERY_ID, required = false) Long queryId,
                                              @RequestParam(name = ExporterConst.QUERY, required = false) String query,
                                              @RequestParam(name = ExporterConst.QUERY_FORMAT, required = false) Format queryFormat,
                                              @RequestParam(name = ExporterConst.QUERY_LABEL, required = false) String queryLabel,
                                              @RequestParam(name = ExporterConst.QUERY_DESCRIPTION, required = false) String queryDescription,
                                              @RequestParam(name = ExporterConst.QUERY_CONTACT_ID, required = false) String queryContactId,
                                              @RequestParam(name = ExporterConst.QUERY_EXPIRATION_DATE, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryExpirationDate,
                                              @RequestParam(name = ExporterConst.OUTPUT_FORMAT) Format outputFormat,
                                              @RequestParam(name = ExporterConst.TEMPLATE_ID, required = false) String templateId,
                                              @RequestHeader(name = "Content-Type", required = false) String contentType,
                                              @RequestHeader(name = ExporterConst.IS_INTERNAL_REQUEST, required = false) Boolean isInternalRequest,
                                              @RequestBody(required = false) String template) {
    /*
    if (!outputFormat.isPath()) {
      return ResponseEntity.badRequest().body("Output format is not a file");
    }*/
        ExporterCoreParameters exporterCoreParameters = null;
        try {
            exporterCoreParameters = exporterCore.extractParameters(
                    new ExporterParameters(queryId, query, templateId, template, contentType, queryFormat,
                            queryLabel, queryDescription, queryContactId, queryExpirationDate, outputFormat));
        } catch (ExporterCoreException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        Long queryExecutionId = exporterDbService.saveQueryExecutionAndGetExecutionId(
                createQueryExecution(exporterCoreParameters));
        final ExporterCoreParameters tempExporterCoreParameters = exporterCoreParameters;
        new Thread(() -> generateFiles(tempExporterCoreParameters, queryExecutionId)).start();
        try {
            return ResponseEntity.ok()
                    .body(createRequestResponseEntity(httpServletRequest, queryExecutionId, isInternalRequest));
        } catch (ExporterControllerException e) {
            return createInternalServerError(e);
        }
    }

    private void generateFiles(ExporterCoreParameters exporterCoreParameters, Long queryExecutionId) {
        try {
            exporterCore.retrieveQuery(exporterCoreParameters).subscribe(
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
    public ResponseEntity<InputStreamResource> getResponse(
            @RequestParam(name = ExporterConst.QUERY_EXECUTION_ID) Long queryExecutionId,
            @RequestParam(name = ExporterConst.FILE_FILTER, required = false) String fileFilter,
            @RequestParam(name = ExporterConst.FILE_COLUMN_PIVOT, required = false) String fileColumnPivot,
            @RequestParam(name = ExporterConst.PAGE, required = false) Integer pageCounter,
            @RequestParam(name = ExporterConst.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = ExporterConst.PIVOT_VALUE, required = false) String pivotValue,
            @RequestParam(name = ExporterConst.MERGE_FILES, required = false) Boolean mergeFiles) {
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
                                queryExecutionId, fileFilter, fileColumnPivot, pageCounter, pivotValue, pageSize, mergeFiles);
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
            String pivotValue, Integer pageSize, Boolean mergeFiles)
            throws ExporterControllerException, ZipperException, IOException, ExplorerException, PivotIdentifierException {
        List<QueryExecutionFile> queryExecutionFiles = exporterDbService.fetchQueryExecutionFilesByQueryExecutionId(
                queryExecutionFileId);
        List<Path> files = convertToPath(queryExecutionFiles);
        HttpHeaders httpHeaders = new HttpHeaders();
        if (fileColumnPivot != null && (pageCounter != null || pivotValue != null)) {
            FilesAndNumberOfPagesOfPivot filesAndNumberOfPagesOfPivot = filterFiles(files, fileColumnPivot, pageCounter, pivotValue, pageSize);
            files = filesAndNumberOfPagesOfPivot.paths();
            httpHeaders.add(ExporterConst.NUMBER_OF_PAGES, String.valueOf(filesAndNumberOfPagesOfPivot.numberOfPagesOfPivot()));
        }
        files = filterFiles(files, fileFilter);
        if (files.size() > 0) {
            if (files.size() == 1) {
                return createResponseEntity(files.get(0), httpHeaders);
            } else if (mergeFiles != null && mergeFiles == true && filesMergerManager.isFileExtensionSupported(files)) {
                Optional<Path> mergedFile = filesMergerManager.merge(files);
                return (mergedFile.isPresent()) ? createResponseEntity(mergedFile.get(), httpHeaders) : ResponseEntity.notFound().build();
            } else {
                Pair<InputStreamResource, String> inputStreamResourceFilenamePair = zipper.zipFiles(files);
                return createResponseEntity(inputStreamResourceFilenamePair.getFirst(),
                        fetchFilename(inputStreamResourceFilenamePair.getSecond()), httpHeaders);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<InputStreamResource> createResponseEntity(Path path, HttpHeaders httpHeaders) throws FileNotFoundException {
        return createResponseEntity(new InputStreamResource(new FileInputStream(path.toAbsolutePath().toString())),
                fetchFilename(path.getFileName().toString()), httpHeaders);
    }

    private List<Path> convertToPath(List<QueryExecutionFile> queryExecutionFiles) {
        return queryExecutionFiles.stream().map(queryExecutionFile -> Path.of(queryExecutionFile.getFilePath())).toList();
    }

    private FilesAndNumberOfPagesOfPivot filterFiles(List<Path> files, String fileColumnPivot, Integer pageCounter, String pivotValue, Integer pageSize) throws PivotIdentifierException, ExplorerException {
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
                    result.add(explorer.get().filter(file, pivots));
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

    @GetMapping(value = ExporterConst.RETRIEVE_QUERY, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public ResponseEntity<Flux<Path>> retrieveQuery(
            @RequestParam(name = ExporterConst.QUERY_ID, required = false) Long queryId,
            @RequestParam(name = ExporterConst.QUERY, required = false) String query,
            @RequestParam(name = ExporterConst.QUERY_FORMAT) Format queryFormat,
            @RequestParam(name = ExporterConst.OUTPUT_FORMAT) Format outputFormat,
            @RequestParam(name = ExporterConst.QUERY_LABEL, required = false) String queryLabel,
            @RequestParam(name = ExporterConst.QUERY_DESCRIPTION, required = false) String queryDescription,
            @RequestParam(name = ExporterConst.QUERY_CONTACT_ID, required = false) String queryContactId,
            @RequestParam(name = ExporterConst.QUERY_EXPIRATION_DATE, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryExpirationDate,
            @RequestParam(name = ExporterConst.TEMPLATE_ID, required = false) String templateId,
            @RequestHeader(name = "Content-Type", required = false) String contentType,
            @RequestBody(required = false) String template) {
        try {
            ExporterCoreParameters exporterCoreParameters = exporterCore.extractParameters(
                    new ExporterParameters(queryId, query, templateId, template, contentType, queryFormat,
                            queryLabel, queryDescription, queryContactId, queryExpirationDate, outputFormat));
            return ResponseEntity.ok().body(exporterCore.retrieveQuery(exporterCoreParameters));
        } catch (ExporterCoreException e) {
            return createInternalServerError(e);
        }
    }

    @GetMapping(value = ExporterConst.LOGS)
    public ResponseEntity<String[]> fetchLogs(
            @RequestParam(name = ExporterConst.LOGS_SIZE) int logsSize,
            @RequestParam(name = ExporterConst.LOGS_LAST_LINE, required = false) String logsLastLine) {
        return ResponseEntity.ok().body(BufferedLoggerFactory.getLastLoggerLines(logsSize, logsLastLine));
    }

    @GetMapping(value = ExporterConst.TEMPLATE_IDS)
    public ResponseEntity<String[]> fetchTemplateIds() {
        return ResponseEntity.ok().body(converterTemplateManager.getConverterTemplateIds().toArray(new String[0]));
    }

    @GetMapping(value = ExporterConst.TEMPLATE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> fetchTemplate(
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


}
