package de.samply.teiler;

public class TeilerConst {

  // HTTP Headers
  public final static String API_KEY_HEADER = "x-api-key";

  // Token variables
  public final static String TOKEN_HEAD = "${";
  public final static String TOKEN_END = "}";
  public final static String TOKEN_EXTENSION_DELIMITER = ":";
  public final static String DEFAULT_TIMESTAMP_FORMAT = "yyyyMMdd-HH_mm";
  public final static String RELATED_FHIR_PATH_DELIMITER = ",";

  // Blaze Store Constants
  public final static String FHIR_STORE_NEXT_BUNDLE = "next";

  // Environment Variables
  public final static String CONVERTER_TEMPLATE_DIRECTORY = "CONVERTER_TEMPLATE_DIRECTORY";
  public final static String WRITE_FILE_DIRECTORY = "WRITE_FILE_DIRECTORY";
  public final static String TEMPORAL_FILE_DIRECTORY = "TEMPORAL_FILE_DIRECTORY";
  public final static String EXCEL_WORKBOOK_WINDOW = "EXCEL_WORKBOOK_WINDOW";
  public final static String CONVERTER_XML_APPLICATION_CONTEXT_PATH = "CONVERTER_XML_APPLICATION_CONTEXT_PATH";
  public final static String TEILER_API_KEY = "TEILER_API_KEY";
  public final static String ZIP_FILENAME = "ZIP_FILENAME";
  public final static String CLEAN_TEMP_FILES_CRON_EXPRESSION = "CLEAN_TEMP_FILES_CRON_EXPRESSION";
  public final static String TEMP_FILES_LIFETIME_IN_DAYS = "TEMP_FILES_LIFETIME_IN_DAYS";
  public final static String CLEAN_WRITE_FILES_CRON_EXPRESSION = "CLEAN_WRITE_FILES_CRON_EXPRESSION";
  public final static String WRITE_FILES_LIFETIME_IN_DAYS = "WRITE_FILES_LIFETIME_IN_DAYS";
  public final static String TEILER_ROOT_CONFIG_URL = "TEILER_ROOT_CONFIG_URL";

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
  public final static String TEILER_API_KEY_SV = HEAD_SV + TEILER_API_KEY + BOTTOM_SV;
  public final static String ZIP_FILENAME_SV =
      HEAD_SV + ZIP_FILENAME + ":#{'teiler-files-${TIMESTAMP}.zip'}" + BOTTOM_SV;
  public final static String CLEAN_TEMP_FILES_CRON_EXPRESSION_SV =
      HEAD_SV + CLEAN_TEMP_FILES_CRON_EXPRESSION + ":#{'0 0 1 * * *'}" + BOTTOM_SV;
  public final static String TEMP_FILES_LIFETIME_IN_DAYS_SV =
      HEAD_SV + TEMP_FILES_LIFETIME_IN_DAYS + ":#{1}" + BOTTOM_SV;
  public final static String CLEAN_WRITE_FILES_CRON_EXPRESSION_SV =
      HEAD_SV + CLEAN_WRITE_FILES_CRON_EXPRESSION + ":#{'0 0 2 * * *'}" + BOTTOM_SV;
  public final static String WRITE_FILES_LIFETIME_IN_DAYS_SV =
      HEAD_SV + WRITE_FILES_LIFETIME_IN_DAYS + ":#{30}" + BOTTOM_SV;

  public final static String TEILER_ROOT_CONFIG_URL_SV =
      HEAD_SV + TEILER_ROOT_CONFIG_URL + ":#{null}" + BOTTOM_SV;

  public final static String[] CROS_ORIGINS = new String[]{TEILER_ROOT_CONFIG_URL_SV};

  // REST Paths
  public static final String INFO = "/info";
  public static final String CREATE_QUERY = "/create-query";
  public static final String QUERIES = "/queries";
  public static final String REQUEST = "/request";
  public static final String RESPONSE = "/response";
  public static final String RETRIEVE_QUERY = "/retrieve-query";
  public static final String INQUIRY = "/inquiry";
  public static final String ACTIVE_INQUIRIES = "/active-inquiries";
  public static final String ARCHIVED_INQUIRIES = "/archived-inquiries";
  public static final String ERROR_INQUIRIES = "/error-inquiries";

  public static final String[] REST_PATHS_WITH_API_KEY = new String[]{CREATE_QUERY, RETRIEVE_QUERY,
      QUERIES /*, REQUEST, ACTIVE_INQUIRIES, ARCHIVED_INQUIRIES, ERROR_INQUIRIES */};
  // TODO: RESPONSE ??? Only with UUID enough?

  // REST Parameters
  public static final String PAGE = "page";
  public static final String PAGE_SIZE = "page-size";
  public static final String STATS = "stats";
  public static final String QUERY_ID = "query-id";
  public static final String QUERY = "query";
  public static final String QUERY_LABEL = "query-label";
  public static final String QUERY_DESCRIPTION = "query-description";
  public static final String QUERY_CONTACT_ID = "query-contact-id";
  public static final String QUERY_EXPIRATION_DATE = "query-expiration-date";
  public static final String TEMPLATE_ID = "template-id";
  public static final String QUERY_FORMAT = "query-format";
  public static final String OUTPUT_FORMAT = "output-format";
  public static final String QUERY_EXECUTION_ID = "query-execution-id";

  // Other constants
  public static final String DEFAULT_CSV_SEPARATOR = "\t";
  public static final String APP_NAME = "Teiler";
  public static final String EMPTY_EXCEL_CELL = "";

  public static final Integer FIRST_ANONYM_ID = 1;
  public static final String DEFAULT_ANONYM_ELEMENT = "VAL";
  public static final String CHILD_FHIR_PATH_HEAD = "/";


}
