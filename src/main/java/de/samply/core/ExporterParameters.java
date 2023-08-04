package de.samply.core;

import de.samply.converter.Format;
import java.time.LocalDate;

public record ExporterParameters(
    Long queryId,
    String query,
    String templateId,
    String template,
    String contentType,
    Format queryFormat,
    String queryLabel,
    String queryDescription,
    String queryContactId,
    LocalDate queryExpirationDate,
    Format outputFormat) {}
