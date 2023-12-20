package de.samply.exporter;

public class ExporterConst {

    public final static boolean LOG_FHIR_VALIDATION_DEFAULT = false;

    // HTTP Headers
    public final static String API_KEY_HEADER = "x-api-key";

    // Token variables
    public final static String TOKEN_HEAD = "${";
    public final static String TOKEN_END = "}";
    public final static String TOKEN_EXTENSION_DELIMITER = ":";
    public final static String DEFAULT_TIMESTAMP_FORMAT = "yyyyMMdd-HH_mm_ss";
    public final static String RELATED_FHIR_PATH_DELIMITER = ",";

    // Blaze Store Constants
    public final static String FHIR_STORE_NEXT_BUNDLE = "next";

    // Opal Constants
    public final static String OPAL_CMD = "opal";
    public final static String OPAL_URL = "--opal";
    public final static String OPAL_PROJECT_NAME = "--name";
    public final static String OPAL_USER = "--user";
    public final static String OPAL_PASSWORD = "--password";
    public final static String OPAL_PROJECT_OP = "project";
    public final static String OPAL_PROJECT_ADD = "--add";
    public final static String OPAL_PROJECT_DATABASE = "--database";
    public final static String OPAL_FILE_OP = "file";
    public final static String OPAL_FILE_UPLOAD = "-up";
    public final static String OPAL_FILE_DELETE = "-dt";
    public final static String OPAL_FILE_FORCE = "--force";
    public final static String OPAL_IMPORT_CSV_OP = "import-csv";
    public final static String OPAL_IMPORT_CSV_PATH = "--path";
    public final static String OPAL_IMPORT_CSV_DESTINATION = "--destination";
    public final static String OPAL_IMPORT_CSV_TABLES = "--tables";
    public final static String OPAL_IMPORT_CSV_SEPARATOR = "--separator";
    public final static String OPAL_IMPORT_CSV_TYPE = "--type";
    public final static String OPAL_IMPORT_CSV_IDENTIFIERS = "--identifiers";
    public final static String OPAL_DEFAULT_VALUE_TYPE = "text";
    public final static String OPAL_VALUE_TYPE = "valueType";
    public final static String OPAL_REST_OP = "rest";
    public final static String OPAL_REST_METHOD = "--method";
    public final static String OPAL_REST_METHOD_GET = "GET";
    public final static String OPAL_REST_METHOD_PUT = "PUT";
    public final static String OPAL_REST_METHOD_POST = "POST";
    public final static String OPAL_REST_CONTENT_TYPE = "--content-type";
    public final static String OPAL_REST_CONTENT_TYPE_JSON = "application/json";
    public final static String OPAL_PATH_PROJECT = "/datasource";
    public final static String OPAL_PATH_TABLE = "/table";
    public final static String OPAL_PATH_VARIABLE = "/variable";
    public final static String OPAL_PATH_VIEW = "/view";
    public final static String OPAL_PATH_VIEWS = "/views";
    public final static String OPAL_DEFAULT_NAMESPACE = "opal";
    public final static String OPAL_ATTRIBUTE_DERIVED_FROM = "derivedFrom";
    public final static String OPAL_ATTRIBUTE_SCRIPT = "script";
    public final static String OPAL_ATTRIBUTE_SCRIPT_ID = "$id()";
    public final static String OPAL_VIEW_SUFFIX = "-view";
    public final static String OPAL_STATUS = "status";
    public final static String OPAL_STATUS_READY = "READY";
    public final static String OPAL_TASK_ID = "id";
    public final static String OPAL_TASK_OP = "task";
    public final static String OPAL_TASK_OP_ID = "--id";
    public final static String OPAL_TASK_OP_WAIT = "--wait";
    public final static String OPAL_PERMISSION_OP = "perm-project";
    public final static String OPAL_PERMISSION_USER_TYPE = "--type";
    public final static String OPAL_PERMISSION = "--permission";
    public final static String OPAL_PERMISSION_PROJECT = "--project";
    public final static String OPAL_PERMISSION_SUBJECT = "--subject";
    public final static String OPAL_PERMISSION_SUBJECT_SEPARATOR = ",";
    public final static String OPAL_PERMISSION_ADD = "--add";


    // Environment Variables
    public final static String CONVERTER_TEMPLATE_DIRECTORY = "CONVERTER_TEMPLATE_DIRECTORY";
    public final static String WRITE_FILE_DIRECTORY = "WRITE_FILE_DIRECTORY";
    public final static String TEMPORAL_FILE_DIRECTORY = "TEMPORAL_FILE_DIRECTORY";
    public final static String EXCEL_WORKBOOK_WINDOW = "EXCEL_WORKBOOK_WINDOW";
    public final static String CONVERTER_XML_APPLICATION_CONTEXT_PATH = "CONVERTER_XML_APPLICATION_CONTEXT_PATH";
    public final static String EXPORTER_API_KEY = "EXPORTER_API_KEY";
    public final static String ZIP_FILENAME = "ZIP_FILENAME";
    public final static String MERGE_FILENAME = "MERGE_FILENAME";
    public final static String CLEAN_TEMP_FILES_CRON_EXPRESSION = "CLEAN_TEMP_FILES_CRON_EXPRESSION";
    public final static String TEMP_FILES_LIFETIME_IN_DAYS = "TEMP_FILES_LIFETIME_IN_DAYS";
    public final static String CLEAN_WRITE_FILES_CRON_EXPRESSION = "CLEAN_WRITE_FILES_CRON_EXPRESSION";
    public final static String ARCHIVE_EXPIRED_QUERIES_CRON_EXPRESSION = "ARCHIVE_EXPIRED_QUERIES_CRON_EXPRESSION";
    public final static String WRITE_FILES_LIFETIME_IN_DAYS = "WRITE_FILES_LIFETIME_IN_DAYS";
    public final static String CROSS_ORIGINS = "CROSS_ORIGINS";
    public final static String TIMEOUT_IN_SECONDS = "TIMEOUT";
    public final static String MAX_NUMBER_OF_RETRIES = "MAX_NUMBER_OF_RETRIES";
    public final static String HTTP_RELATIVE_PATH = "HTTP_RELATIVE_PATH";
    public final static String HTTP_SERVLET_REQUEST_SCHEME = "HTTP_SERVLET_REQUEST_SCHEME";
    public final static String TIMESTAMP_FORMAT = "TIMESTAMP_FORMAT";
    public final static String FHIR_PACKAGES_DIRECTORY = "FHIR_PACKAGES_DIRECTORY";
    public final static String LOG_FHIR_VALIDATION = "LOG_FHIR_VALIDATION";
    public final static String MAX_NUMBER_OF_EXCEL_ROWS_IN_A_SHEET = "MAX_NUMBER_OF_EXCEL_ROWS_IN_A_SHEET";
    public final static String XML_FILE_MERGER_ROOT_ELEMENT = "XML_FILE_MERGER_ROOT_ELEMENT";
    public final static String CSV_SEPARATOR_REPLACEMENT = "CSV_SEPARATOR_REPLACEMENT";

    // Spring Values (SV)
    public final static String HEAD_SV = "${";
    public final static String BOTTOM_SV = "}";
    public final static String CONVERTER_TEMPLATE_DIRECTORY_SV =
            HEAD_SV + CONVERTER_TEMPLATE_DIRECTORY
                    + ":#{'./templates'}" + BOTTOM_SV;
    public final static String WRITE_FILE_DIRECTORY_SV =
            HEAD_SV + WRITE_FILE_DIRECTORY + ":#{'./output'}" + BOTTOM_SV;
    public final static String TEMPORAL_FILE_DIRECTORY_SV =
            HEAD_SV + TEMPORAL_FILE_DIRECTORY + ":#{'./temp-files'}" + BOTTOM_SV;
    public final static String EXCEL_WORKBOOK_WINDOW_SV =
            HEAD_SV + EXCEL_WORKBOOK_WINDOW + ":#{'30000000'}" + BOTTOM_SV;
    public final static String CONVERTER_XML_APPLICATION_CONTEXT_PATH_SV =
            HEAD_SV + CONVERTER_XML_APPLICATION_CONTEXT_PATH + ":#{'./converter/converter.xml'}"
                    + BOTTOM_SV;
    public final static String EXPORTER_API_KEY_SV = HEAD_SV + EXPORTER_API_KEY + BOTTOM_SV;
    public final static String ZIP_FILENAME_SV =
            HEAD_SV + ZIP_FILENAME + ":#{'exporter-files-${TIMESTAMP}.zip'}" + BOTTOM_SV;
    public final static String CLEAN_TEMP_FILES_CRON_EXPRESSION_SV =
            HEAD_SV + CLEAN_TEMP_FILES_CRON_EXPRESSION + ":#{'0 0 1 * * *'}" + BOTTOM_SV;
    public final static String TEMP_FILES_LIFETIME_IN_DAYS_SV =
            HEAD_SV + TEMP_FILES_LIFETIME_IN_DAYS + ":#{1}" + BOTTOM_SV;
    public final static String CLEAN_WRITE_FILES_CRON_EXPRESSION_SV =
            HEAD_SV + CLEAN_WRITE_FILES_CRON_EXPRESSION + ":#{'0 0 2 * * *'}" + BOTTOM_SV;
    public final static String ARCHIVE_EXPIRED_QUERIES_CRON_EXPRESSION_SV =
            HEAD_SV + ARCHIVE_EXPIRED_QUERIES_CRON_EXPRESSION + ":#{'0 0 2 * * *'}" + BOTTOM_SV;
    public final static String WRITE_FILES_LIFETIME_IN_DAYS_SV =
            HEAD_SV + WRITE_FILES_LIFETIME_IN_DAYS + ":#{30}" + BOTTOM_SV;
    public final static String CROSS_ORIGINS_SV =
            "#{'" + HEAD_SV + CROSS_ORIGINS + ":#{null}" + BOTTOM_SV + "'.split(',')}";
    public final static String HTTP_RELATIVE_PATH_SV =
            HEAD_SV + HTTP_RELATIVE_PATH + ":" + BOTTOM_SV;
    public final static String HTTP_SERVLET_REQUEST_SCHEME_SV =
            HEAD_SV + HTTP_SERVLET_REQUEST_SCHEME + ":http" + BOTTOM_SV;
    public final static String TIMESTAMP_FORMAT_SV =
            HEAD_SV + TIMESTAMP_FORMAT + ":" + DEFAULT_TIMESTAMP_FORMAT + BOTTOM_SV;
    public final static String FHIR_PACKAGES_DIRECTORY_SV =
            HEAD_SV + FHIR_PACKAGES_DIRECTORY + ":#{'./fhir-packages'}" + BOTTOM_SV;
    public final static String LOG_FHIR_VALIDATION_SV =
            HEAD_SV + LOG_FHIR_VALIDATION + ":" + LOG_FHIR_VALIDATION_DEFAULT + BOTTOM_SV;
    public final static String MAX_NUMBER_OF_EXCEL_ROWS_IN_A_SHEET_SV =
            HEAD_SV + MAX_NUMBER_OF_EXCEL_ROWS_IN_A_SHEET + ":100000" + BOTTOM_SV;
    public final static String MERGE_FILENAME_SV =
            HEAD_SV + MERGE_FILENAME + ":#{'exporter-files-${TIMESTAMP}'}" + BOTTOM_SV;
    public final static String XML_FILE_MERGER_ROOT_ELEMENT_SV =
            HEAD_SV + XML_FILE_MERGER_ROOT_ELEMENT + ":Containers" + BOTTOM_SV;
    public final static String CSV_SEPARATOR_REPLACEMENT_SV =
            HEAD_SV + CSV_SEPARATOR_REPLACEMENT + ": " + BOTTOM_SV;



    // REST Paths
    public static final String INFO = "/info";
    public static final String CREATE_QUERY = "/create-query";
    public static final String UPDATE_QUERY = "/update-query";
    public static final String FETCH_QUERIES = "/queries";
    public static final String FETCH_QUERY_EXECUTIONS = "/query-executions";
    public static final String FETCH_QUERY_EXECUTION_ERRORS = "/query-execution-errors";
    public static final String REQUEST = "/request";
    public static final String RESPONSE = "/response";
    public static final String INQUIRY = "/inquiry";
    public static final String STATUS = "/status";
    public static final String ARCHIVE_QUERY = "/archive-query";
    public static final String ACTIVE_INQUIRIES = "/active-inquiries";
    public static final String ARCHIVED_INQUIRIES = "/archived-inquiries";
    public static final String ERROR_INQUIRIES = "/error-inquiries";
    public static final String LOGS = "/logs";
    public static final String TEMPLATE_IDS = "/template-ids";
    public static final String TEMPLATE = "/template";
    public static final String INPUT_FORMATS = "/input-formats";
    public static final String OUTPUT_FORMATS = "/output-formats";
    public static final String TEMPLATE_GRAPH = "/template-graph";
    public static final String RUNNING_QUERIES = "/running-queries";

    public static final String[] REST_PATHS_WITH_API_KEY = new String[]{CREATE_QUERY, FETCH_QUERIES,
            FETCH_QUERY_EXECUTIONS, FETCH_QUERY_EXECUTION_ERRORS, REQUEST, ACTIVE_INQUIRIES, ARCHIVED_INQUIRIES,
            ERROR_INQUIRIES, INQUIRY, ARCHIVE_QUERY, STATUS, LOGS, RUNNING_QUERIES, UPDATE_QUERY};
    // TODO: RESPONSE ??? Only with UUID enough?

    // REST Headers
    public static final String NUMBER_OF_PAGES = "number-of-pages";

    // REST Parameters
    public static final String PAGE = "page";
    public static final String PAGE_SIZE = "page-size";
    public static final String STATS = "stats";
    public static final String QUERY_ID = "query-id";
    public static final String QUERY = "query";
    public static final String QUERY_LABEL = "query-label";
    public static final String QUERY_DESCRIPTION = "query-description";
    public static final String QUERY_CONTACT_ID = "query-contact-id";
    public static final String QUERY_EXECUTION_CONTACT_ID = "query-execution-contact-id";
    public static final String QUERY_EXPIRATION_DATE = "query-expiration-date";
    public static final String QUERY_DEFAULT_OUTPUT_FORMAT = "query-default-output-format";
    public static final String QUERY_DEFAULT_TEMPLATE_ID = "query-default-template-id";
    public static final String TEMPLATE_ID = "template-id";
    public static final String QUERY_CONTEXT = "query-context";
    public static final String QUERY_FORMAT = "query-format";
    public static final String OUTPUT_FORMAT = "output-format";
    public static final String QUERY_EXECUTION_ID = "query-execution-id";
    public static final String FILE_FILTER = "file-filter";
    public static final String FILE_COLUMN_PIVOT = "pivot";
    public static final String PIVOT_VALUE = "pivot-value";
    public static final String LOGS_SIZE = "logs-size";
    public static final String LOGS_LAST_LINE = "logs-last-line";
    public final static String IS_INTERNAL_REQUEST = "internal-request";
    public final static String MERGE_FILES = "merge-files";
    public final static String FILE_AS_PLAIN_TEXT_IN_BODY = "in-body";


    /*
    public static final List<String> CROSS_PARAMETERS = Arrays.asList(PAGE, PAGE_SIZE, STATS,
        QUERY_ID, QUERY, QUERY_LABEL, QUERY_DESCRIPTION, QUERY_CONTACT_ID, QUERY_EXPIRATION_DATE,
        TEMPLATE_ID, QUERY_FORMAT, OUTPUT_FORMAT, QUERY_EXECUTION_ID);
  */
    // Other constants
    public static final String DEFAULT_CSV_SEPARATOR = "\t";
    public static final String FILE_FILTER_SEPARATOR = ",";
    public static final String APP_NAME = "Exporter";
    public static final String EMPTY_EXCEL_CELL = "";

    public static final Integer FIRST_ANONYM_ID = 1;
    public static final String DEFAULT_ANONYM_ELEMENT = "VAL";
    public static final String CHILD_FHIR_PATH_HEAD = "/";

    public static final String INQUIRY_NULL_LABEL = "Anonym";
    public static final String INQUIRY_NULL_DESCRIPTION = "No description provided";
    public static final String CROSS_ORIGINS_SEPARATOR = ",";
    public static final int DEFAULT_TIMEOUT_IN_SECONDS = 5;
    public static final int DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS = 10;
    public static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT_IN_SECONDS = 20;
    public static final int DEFAULT_SOCKET_TIMEOUT_IN_SECONDS = 30;
    public static final int DEFAULT_MAX_NUMBER_OF_RETRIES = 10;
    public static final String[] PASSWORD_BLACKLIST = {"password", "secret", "apikey"};
    public static final String PASSWORD_REPLACEMENT = "XXXXX";
    public static final String VALIDATION_MESSAGE_SEPARATOR = " | ";
    public static final String FHIR_PACKAGE_ROOT_CLASSPATH = "fhir-packages";
    public static final String HTTP_HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HTTP_HEADER_CONTENT_DISPOSITION_FILENAME = "filename=\"";
    public static final int RANDOM_FILENAME_SIZE = 10;
    public static final String BLAZE_URL_QUERY_PARAMETER_PAGE_SIZE = "_count=";
    public static final int DEFAULT_FHIR_PAGE_SIZE = 50;
    public final static int BUFFERED_LOGGER_SIZE = 1000;
    public final static String JSON_FILE_EXTENSION = "json";
    public final static String XML_FILE_EXTENSION = "xml";
    public final static String MEASURE_REPORT_GROUP_POPULATION_SUBJECT_RESULTS_REFERENCE_PREFIX = "List/";
    public final static String FHIR_SEARCH_LIST_PARAMETER = "_list";
    public final static String DEFAULT_GRAPH_FORMAT = "JSON";
    public final static String ERROR_MESSAGE_INTERRUPTED_QUERY_EXECUTION = "Query execution interrupted abruptly";
    public final static String QUERY_CONTEXT_SEPARATOR = ";";
    public final static String QUERY_CONTEXT_EQUAL = "=";

}
