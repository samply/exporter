package de.samply.converter;

import de.samply.template.conversion.ConversionTemplate;
import reactor.core.publisher.Flux;

public abstract class ConverterImpl<I,O> implements Converter<I,O>{

  protected abstract Flux<O> convert(I input, ConversionTemplate template);
  @Override
  public Flux<O> convert(Flux<I> inputFlux, ConversionTemplate template) {
    return inputFlux.flatMap(input -> convert(input, template));
  }

}
