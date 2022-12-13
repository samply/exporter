package de.samply.query;

import de.samply.result.container.template.ResultTemplate;
import reactor.core.publisher.Flux;

public interface Converter<I,O> {
  Flux<O> convert (Flux<I> input, ResultTemplate template);
}
