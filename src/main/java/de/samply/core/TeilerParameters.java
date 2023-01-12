package de.samply.core;

import de.samply.converter.Format;
import de.samply.teiler.TeilerConst;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

public record TeilerParameters(
    Long queryId,
    String query,
    String sourceId,
    String templateId,
    String template,
    String contentType,
    Format queryFormat,
    String queryLabel,
    String queryDescription,
    String queryContactId,
    LocalDate queryExpirationDate,
    Format outputFormat) {}
