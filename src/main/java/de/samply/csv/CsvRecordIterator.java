package de.samply.csv;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CsvRecordIterator implements Iterator<CSVRecord>, Closeable {

  private final FileReader fileReader;
  private final BufferedReader bufferedReader;
  private final CSVParser csvParser;
  private final Iterator<CSVRecord> iterator;

  public CsvRecordIterator(Path sourcePath, CsvConfig csvConfig) throws IOException {
    fileReader = new FileReader(sourcePath.toFile());
    bufferedReader = new BufferedReader(fileReader);
    csvParser = createCsvParser(bufferedReader, csvConfig);
    iterator = csvParser.iterator();
  }

  private CSVParser createCsvParser(BufferedReader bufferedReader, CsvConfig csvConfig)
      throws IOException {
    return Builder
        .create()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setDelimiter(csvConfig.delimiter())
        .setRecordSeparator(csvConfig.endOfLine())
        .build()
        .parse(bufferedReader);
  }

  @Override
  public boolean hasNext() {
    boolean result = iterator.hasNext();
    if (!result) {
      try {
        close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  @Override
  public void close() throws IOException {
    if (csvParser != null && !csvParser.isClosed()) {
      csvParser.close();
    }
    if (bufferedReader != null) {
      bufferedReader.close();
    }
    if (fileReader != null) {
      fileReader.close();
    }
  }

  @Override
  public CSVRecord next() {
    return iterator.next();
  }

}
