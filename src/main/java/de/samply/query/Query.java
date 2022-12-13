package de.samply.query;

import de.samply.converter.Format;

public record Query(
    String queryId,
    String query,
    Format queryFormat
) {

}
