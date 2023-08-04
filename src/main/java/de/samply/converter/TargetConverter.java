package de.samply.converter;

public interface TargetConverter<I, O> extends Converter<I, O> {

  String getTargetId();

}
