package de.samply.csv;

import java.nio.charset.Charset;

public record CsvConfig(
    Charset charset,
    String endOfLine,
    String delimiter
) {

}
