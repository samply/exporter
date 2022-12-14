package de.samply.converter;

import de.samply.template.conversion.ConversionTemplate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Flux;

public class ConverterGroup<I,O> implements Converter<I,O>{
  private List<Converter> converters;

  public ConverterGroup(List<Converter> converters) {
    this.converters = converters;
  }

  @Override
  public Flux<O> convert(Flux<I> input, ConversionTemplate template) {
    AtomicReference<Flux> tempFlux = new AtomicReference<>(input);
    converters.forEach(converter -> tempFlux.set(converter.convert(tempFlux.get(), template)));
    return tempFlux.get();
  }

}
