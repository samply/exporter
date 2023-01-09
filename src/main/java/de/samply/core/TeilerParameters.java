package de.samply.core;

import de.samply.converter.Format;

public record TeilerParameters(
    String queryId,
    String query,
    String sourceId,
    String templateId,
    String template,
    String contentType,
    Format queryFormat,
    Format outputFormat) {}
