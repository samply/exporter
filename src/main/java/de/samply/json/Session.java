package de.samply.json;

import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplateUtils;
import de.samply.template.token.TokenContext;

public class Session extends de.samply.files.Session {

    public Session(ConverterTemplateUtils converterTemplateUtils, String writeDirectory, TokenContext tokenContext) {
        super(converterTemplateUtils, writeDirectory, tokenContext);
    }

    @Override
    protected String getFilename(ContainerTemplate containerTemplate) {
        return containerTemplate.getJsonFilename();
    }

}
