package de.samply.converter;

import de.samply.template.ConverterTemplate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

public class ConverterGroup<I, O> implements Converter<I, O> {

  private List<Converter> converters;

  public ConverterGroup(List<Converter> converters) {
    this.converters = converters;
  }

  @Override
  public Flux<O> convert(Flux<I> input, ConverterTemplate template, HttpServletRequest httpServletRequest) {
    AtomicReference<Flux> tempFlux = new AtomicReference<>(input);
    converters.forEach(converter -> tempFlux.set(converter.convert(tempFlux.get(), template, httpServletRequest)));
    return tempFlux.get();
  }

  @Override
  public Format getInputFormat() {
    return (converters != null && converters.size() > 0) ? converters.get(0).getInputFormat()
        : null;
  }

  @Override
  public Format getOutputFormat() {
    return (converters != null && converters.size() > 0) ? converters.get(converters.size() - 1)
        .getOutputFormat() : null;
  }

  public List<Converter> getConverters() {
    return converters;
  }

}
