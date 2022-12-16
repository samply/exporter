package de.samply.converter;

public interface SourceConverter<I,O> extends Converter<I,O> {

  String getSourceId();

}
