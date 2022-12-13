package de.samply.query;

import de.samply.result.container.template.ContainersTemplate;
import reactor.core.publisher.Flux;

public interface QueryExecutor<R> {
  Flux<R> execute (String query, ContainersTemplate template);
}
