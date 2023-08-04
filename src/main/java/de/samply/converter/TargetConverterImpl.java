package de.samply.converter;

import de.samply.converter.ConverterImpl;
import de.samply.converter.TargetConverter;

public abstract class TargetConverterImpl<I, O, S> extends ConverterImpl<I, O, S> implements
    TargetConverter<I, O> {

}
