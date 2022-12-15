package de.samply.converter;

import de.samply.template.ConverterTemplate;
import reactor.core.publisher.Flux;

public abstract class ConverterImpl<I,O> implements Converter<I,O>{

  protected abstract Flux<O> convert(I input, ConverterTemplate template);
  @Override
  public Flux<O> convert(Flux<I> inputFlux, ConverterTemplate template) {
    return inputFlux.flatMap(input -> convert(input, template));
  }

}
