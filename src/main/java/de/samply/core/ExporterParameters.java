package de.samply.core;

import de.samply.converter.Format;
import java.time.LocalDate;

public record ExporterParameters(
    Long queryId,
    String query,
    String templateId,
    String requestTemplate,
    String contentType,
    Format queryFormat,
    String queryLabel,
    String queryDescription,
    String queryContactId,
    String queryContext,
    String queryExecutionContactId,
    LocalDate queryExpirationDate,
    Format outputFormat) {}
