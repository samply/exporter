package de.samply.fhir.cql;

public class CqlExtractorException extends Exception {

    public CqlExtractorException(String message) {
        super(message);
    }

    public CqlExtractorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CqlExtractorException(Throwable cause) {
        super(cause);
    }

    public CqlExtractorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
