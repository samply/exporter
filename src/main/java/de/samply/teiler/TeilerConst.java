package de.samply.teiler;

public class TeilerConst {

    // Blaze Store Constants
    public final static String FHIR_STORE_NEXT_BUNDLE = "next";

    // Environment Variables
    public final static String CONVERTER_TEMPLATE_DIRECTORY = "CONVERTER_TEMPLATE_DIRECTORY";
    public final static String WRITE_FILE_DIRECTORY = "WRITE_FILE_DIRECTORY";
    public final static String CONVERTER_XML_APPLICATION_CONTEXT_PATH = "CONVERTER_XML_APPLICATION_CONTEXT_PATH";

    // Spring Values (SV)
    public final static String HEAD_SV = "${";
    public final static String BOTTOM_SV = "}";
    public final static String CONVERTER_TEMPLATE_DIRECTORY_SV = HEAD_SV + CONVERTER_TEMPLATE_DIRECTORY
        + ":#{'./templates'}" + BOTTOM_SV;
    public final static String WRITE_FILE_DIRECTORY_SV = HEAD_SV + WRITE_FILE_DIRECTORY + ":#{'./output'}" + BOTTOM_SV;
    public final static String CONVERTER_XML_APPLICATION_CONTEXT_PATH_SV = HEAD_SV + CONVERTER_XML_APPLICATION_CONTEXT_PATH + ":#{'./converter/converter.xml'}" + BOTTOM_SV;

    // REST Paths
    public static final String INFO = "info";
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    public static final String RETRIEVE_QUERY = "retrieve-query";
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

}
