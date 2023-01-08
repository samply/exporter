package de.samply.teiler;

import de.samply.converter.Format;
import de.samply.core.TeilerCore;
import de.samply.core.TeilerCoreException;
import de.samply.utils.ProjectVersion;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
  public Flux<Path> getResponse(@RequestParam(TeilerConst.QUERY_ID) String queryId) {
    //TODO
    return null;
  }

  @GetMapping(value = TeilerConst.RETRIEVE_QUERY, produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<Path> retrieveQuery(
      @RequestParam(TeilerConst.QUERY) String query,
      @RequestParam(TeilerConst.SOURCE_ID) String sourceId,
      @RequestParam(TeilerConst.TEMPLATE_ID) String templateId,
      @RequestParam(TeilerConst.QUERY_FORMAT) Format queryFormat,
      @RequestParam(TeilerConst.OUTPUT_FORMAT) Format outputFormat
  ) {
    try {
      return teilerCore.retrieveByQuery(sourceId, query, queryFormat, outputFormat, templateId);
    } catch (TeilerCoreException e) {
      //TODO
      throw new RuntimeException(e);
    }
  }


}
