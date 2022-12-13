package de.samply.query;

public record Query(
    String queryId,
    String query,
    QueryFormat queryFormat
) {}
