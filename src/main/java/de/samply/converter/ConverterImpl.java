package de.samply.converter;

import de.samply.template.ConverterTemplate;
import reactor.core.publisher.Flux;

public abstract class ConverterImpl<I,O,S> implements Converter<I,O>{

  protected abstract Flux<O> convert(I input, ConverterTemplate template, S session);
  protected abstract S initializeSession();
  @Override
  public Flux<O> convert(Flux<I> inputFlux, ConverterTemplate template) {
    S session = initializeSession();
    return inputFlux.flatMap(input -> convert(input, template, session));
  }

}
