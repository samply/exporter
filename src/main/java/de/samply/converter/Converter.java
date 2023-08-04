package de.samply.converter;

import de.samply.template.ConverterTemplate;
import reactor.core.publisher.Flux;

public interface Converter<I,O> {
  Flux<O> convert (Flux<I> input, ConverterTemplate template);
  Format getInputFormat();
  Format getOutputFormat();
}
