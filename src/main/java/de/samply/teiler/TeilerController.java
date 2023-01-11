package de.samply.teiler;

import de.samply.converter.Format;
import de.samply.core.TeilerCore;
import de.samply.core.TeilerCoreException;
import de.samply.core.TeilerParameters;
import de.samply.db.crud.TeilerDbService;
import de.samply.db.model.Query;
import de.samply.utils.ProjectVersion;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class TeilerController {

  private final String projectVersion = ProjectVersion.getProjectVersion();
  private final TeilerCore teilerCore;
  private TeilerDbService teilerDbService;

  public TeilerController(@Autowired TeilerCore teilerCore, @Autowired TeilerDbService teilerDbService) {
    this.teilerCore = teilerCore;
    this.teilerDbService = teilerDbService;
  }

  @GetMapping(value = TeilerConst.INFO)
  public ResponseEntity<String> info() {
    return new ResponseEntity<>(projectVersion, HttpStatus.OK);
  }

  @PostMapping(TeilerConst.CREATE_QUERY)
  public String createQuery(
      @RequestParam(name = TeilerConst.QUERY) String query,
      @RequestParam(name = TeilerConst.QUERY_FORMAT) Format queryFormat,
      @RequestParam(name = TeilerConst.QUERY_LABEL) String queryLabel,
      @RequestParam(name = TeilerConst.QUERY_DESCRIPTION) String queryDescription,
      @RequestParam(name = TeilerConst.QUERY_CONTACT_ID) String queryContactId,
      @RequestParam(name = TeilerConst.QUERY_EXPIRATION_DATE)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryExpirationDate
  ) {
    Query tempQuery = new Query();
    tempQuery.setQuery(query);
    tempQuery.setFormat(queryFormat);
    tempQuery.setLabel(queryLabel);
    tempQuery.setDescription(queryDescription);
    tempQuery.setContactId(queryContactId);
    tempQuery.setExpirationDate(queryExpirationDate);
    tempQuery.setCreatedAt(Instant.now());
    return teilerDbService.saveQueryAndGetQueryId(tempQuery);
  }

  @GetMapping(value = TeilerConst.RESPONSE, produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<Path> getResponse() {
    //TODO
    return null;
  }

  @GetMapping(value = TeilerConst.RETRIEVE_QUERY, produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<Path> retrieveQuery(
      @RequestParam(name = TeilerConst.QUERY_ID, required = false) String queryId,
      @RequestParam(name = TeilerConst.QUERY, required = false) String query,
      @RequestParam(name = TeilerConst.SOURCE_ID) String sourceId,
      @RequestParam(name = TeilerConst.QUERY_FORMAT) Format queryFormat,
      @RequestParam(name = TeilerConst.OUTPUT_FORMAT) Format outputFormat,
      @RequestParam(name = TeilerConst.TEMPLATE_ID, required = false) String templateId,
      @RequestHeader(name = "Content-Type", required = false) String contentType,
      @RequestBody(required = false) String template
  ) {
    try {
      return teilerCore.retrieveQuery(
          new TeilerParameters(queryId, query, sourceId, templateId, template, contentType,
              queryFormat, outputFormat));
    } catch (TeilerCoreException e) {
      //TODO
      throw new RuntimeException(e);
    }
  }


}
