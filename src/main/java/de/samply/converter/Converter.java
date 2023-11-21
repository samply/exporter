package de.samply.converter;

import de.samply.template.ConverterTemplate;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

public interface Converter<I,O> {
  Flux<O> convert (Flux<I> input, ConverterTemplate template, HttpServletRequest httpServletRequest);
  Format getInputFormat();
  Format getOutputFormat();
}
