package de.samply.core;

import de.samply.converter.Converter;
import de.samply.db.model.Query;
import de.samply.template.ConverterTemplate;

public record ExporterCoreParameters(
    Query query,
    ConverterTemplate template,
    Converter converter,
    String queryExecutionContactId
) {
}
