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
  public final static String EXCEL_WORKBOOK_WINDOW = "EXCEL_WORKBOOK_WINDOW";
  public final static String CONVERTER_XML_APPLICATION_CONTEXT_PATH = "CONVERTER_XML_APPLICATION_CONTEXT_PATH";
  public final static String TEILER_API_KEY = "TEILER_API_KEY";

  // Spring Values (SV)
  public final static String HEAD_SV = "${";
  public final static String BOTTOM_SV = "}";
  public final static String CONVERTER_TEMPLATE_DIRECTORY_SV =
      HEAD_SV + CONVERTER_TEMPLATE_DIRECTORY
          + ":#{'./templates'}" + BOTTOM_SV;
  public final static String WRITE_FILE_DIRECTORY_SV =
      HEAD_SV + WRITE_FILE_DIRECTORY + ":#{'./output'}" + BOTTOM_SV;
  public final static String EXCEL_WORKBOOK_WINDOW_SV =
      HEAD_SV + EXCEL_WORKBOOK_WINDOW + ":#{'30000000'}" + BOTTOM_SV;
  public final static String CONVERTER_XML_APPLICATION_CONTEXT_PATH_SV =
      HEAD_SV + CONVERTER_XML_APPLICATION_CONTEXT_PATH + ":#{'./converter/converter.xml'}"
          + BOTTOM_SV;
  public final static String TEILER_API_KEY_SV = HEAD_SV + TEILER_API_KEY + BOTTOM_SV;

  // REST Paths
  public static final String INFO = "/info";
  public static final String REQUEST = "/request";
  public static final String RESPONSE = "/response";
  public static final String RETRIEVE_QUERY = "/retrieve-query";
  public static final String PAGE = "page";
  public static final String STATS = "stats";
  public static final String QUERY_ID = "query-id";
  public static final String QUERY = "query";
  public static final String SOURCE_ID = "source-id";
  public static final String TEMPLATE_ID = "template-id";
  public static final String QUERY_FORMAT = "query-format";
  public static final String OUTPUT_FORMAT = "output-format";

  // Other constants
  public static final String DEFAULT_CSV_SEPARATOR = "\t";
  public static final String APP_NAME = "Teiler";
  public static final String EMPTY_EXCEL_CELL = "";

  public static final Integer FIRST_ANONYM_ID = 0;
  public static final String DEFAULT_ANONYM_ELEMENT = "VAL";
  public static final String DEFAULT_QUERY_ID = "EMBEDDED_QUERY";
  public static final String CHILD_FHIR_PATH_HEAD = "/";

}
