package de.samply.teiler;

public class TeilerConst {

    // Blaze Store Constants
    public final static String BLAZE_STORE_API_PATH = "/fhir";

    // Environment Variables
    public final static String BLAZE_STORE_URL = "BLAZE_STORE_URL";
    public final static String RESULT_TEMPLATE_DIRECTORY = "RESULT_TEMPLATE_DIRECTORY";

    // Spring Values (SV)
    public final static String HEAD_SV = "${";
    public final static String BOTTOM_SV = "}";
    public final static String BLAZE_STORE_URL_SV = HEAD_SV + BLAZE_STORE_URL + ":#{null}" + BOTTOM_SV;
    public final static String RESULT_TEMPLATE_DIRECTORY_SV = HEAD_SV + RESULT_TEMPLATE_DIRECTORY + ":#{'./templates'}" + BOTTOM_SV;

    // REST Paths
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    public static final String PAGE = "page";
    public static final String STATS = "stats";
    public static final String QUERY_ID = "queryId";

}
