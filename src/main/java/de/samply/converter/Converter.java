package de.samply.converter;

import de.samply.template.conversion.ConversionTemplate;
import reactor.core.publisher.Flux;

public interface Converter<I,O> {
  Flux<O> convert (Flux<I> input, ConversionTemplate template);
}
