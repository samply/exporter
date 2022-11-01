package de.samply.teiler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {


  @PostMapping(TeilerConst.REQUEST)
  public String createRequest() {
    //TODO
    return null;
  }

  @GetMapping(TeilerConst.RESPONSE)
  public String getResponse(
      @RequestParam(TeilerConst.QUERY_ID) String queryId,
      @RequestParam(TeilerConst.PAGE) Integer page) {

    //TODO
    return null;
  }
}
