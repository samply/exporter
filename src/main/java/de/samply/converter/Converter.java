package de.samply.converter;

import de.samply.template.ConverterTemplate;
import de.samply.template.token.TokenContext;
import reactor.core.publisher.Flux;

public interface Converter<I, O> {
    Flux<O> convert(Flux<I> input, ConverterTemplate template, TokenContext tokenContext);

    Format getInputFormat();

    Format getOutputFormat();
}
