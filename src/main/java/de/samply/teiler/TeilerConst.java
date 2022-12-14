package de.samply.teiler;

public class TeilerConst {

    // Blaze Store Constants
    public final static String FHIR_STORE_NEXT_BUNDLE = "next";

    // Environment Variables
    public final static String CONVERTER_TEMPLATE_DIRECTORY = "CONVERTER_TEMPLATE_DIRECTORY";
    public final static String WRITE_FILE_DIRECTORY = "WRITE_FILE_DIRECTORY";
    public final static String CONVERTER_PATH = "CONVERTER_PATH";

    // Spring Values (SV)
    public final static String HEAD_SV = "${";
    public final static String BOTTOM_SV = "}";
    public final static String CONVERTER_TEMPLATE_DIRECTORY_SV = HEAD_SV + CONVERTER_TEMPLATE_DIRECTORY
        + ":#{'./templates'}" + BOTTOM_SV;
    public final static String WRITE_FILE_DIRECTORY_SV = HEAD_SV + WRITE_FILE_DIRECTORY + ":#{'./output'}" + BOTTOM_SV;
    public final static String CONVERTER_PATH_SV = HEAD_SV + CONVERTER_PATH + ":#{'./converter/converter.xml'}" + BOTTOM_SV;

    // REST Paths
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    public static final String PAGE = "page";
    public static final String STATS = "stats";
    public static final String QUERY_ID = "queryId";

    // Other constants
    public static final String DEFAULT_CSV_SEPARATOR = "\t";

}
