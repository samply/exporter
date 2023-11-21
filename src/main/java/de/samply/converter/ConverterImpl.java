package de.samply.converter;

import de.samply.template.ConverterTemplate;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

public abstract class ConverterImpl<I, O, S> implements Converter<I, O> {

  protected abstract Flux<O> convert(I input, ConverterTemplate template, S session);

  protected abstract S initializeSession(ConverterTemplate converterTemplate, HttpServletRequest httpServletRequest);

  protected Runnable getSessionCompleter(ConverterTemplate template, S session) {
    return () -> {
    };
  }

  @Override
  public Flux<O> convert(Flux<I> inputFlux, ConverterTemplate template, HttpServletRequest httpServletRequest) {
    S session = initializeSession(template, httpServletRequest);
    return inputFlux.flatMap(input -> convert(input, template, session))
        .doOnComplete(getSessionCompleter(template, session));
  }

}
