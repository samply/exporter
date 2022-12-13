package de.samply.converter;

import de.samply.result.container.template.ResultTemplate;
import reactor.core.publisher.Flux;

public abstract class ConverterImpl<I,O> implements Converter<I,O>{

  protected abstract Flux<O> convert(I input, ResultTemplate template);
  @Override
  public Flux<O> convert(Flux<I> inputFlux, ResultTemplate template) {
    return inputFlux.flatMap(input -> convert(input, template));
  }

}
