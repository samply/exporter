package de.samply.teiler;

import de.samply.converter.Format;
import de.samply.core.TeilerCore;
import de.samply.core.TeilerCoreException;
import de.samply.core.TeilerParameters;
import de.samply.utils.ProjectVersion;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SearchController {

  private final String projectVersion = ProjectVersion.getProjectVersion();
  private final TeilerCore teilerCore;

  public SearchController(@Autowired TeilerCore teilerCore) {
    this.teilerCore = teilerCore;
  }

  @GetMapping(value = TeilerConst.INFO)
  public ResponseEntity<String> info() {
    return new ResponseEntity<>(projectVersion, HttpStatus.OK);
  }

  @PostMapping(TeilerConst.REQUEST)
  public String createRequest() {
    //TODO
    return null;
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
