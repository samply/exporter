package de.samply.converter;

import de.samply.template.ConverterTemplate;
import de.samply.template.token.TokenContext;
import reactor.core.publisher.Flux;

public abstract class ConverterImpl<I, O, S> implements Converter<I, O> {

    protected abstract Flux<O> convert(I input, ConverterTemplate template, S session);

    protected abstract S initializeSession(ConverterTemplate converterTemplate, TokenContext tokenContext);

    protected Runnable getSessionCompleter(ConverterTemplate template, S session) {
        return () -> {
        };
    }

    @Override
    public Flux<O> convert(Flux<I> inputFlux, ConverterTemplate template, TokenContext tokenContext) {
        S session = initializeSessionWithSync(template, tokenContext);
        return inputFlux.flatMap(input -> convertWithSync(input, template, session))
                .doOnComplete(getSessionCompleter(template, session));
    }

    private synchronized S initializeSessionWithSync(ConverterTemplate converterTemplate, TokenContext tokenContext){
        return initializeSession(converterTemplate, tokenContext);
    }

    private synchronized Flux<O> convertWithSync(I input, ConverterTemplate template, S session){
        return convert(input, template, session);
    }


}
