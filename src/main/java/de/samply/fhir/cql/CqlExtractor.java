package de.samply.fhir.cql;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.template.ConverterTemplate;
import de.samply.utils.Base64Utils;

public class CqlExtractor {

    private final static Logger logger = BufferedLoggerFactory.getLogger(CqlExtractor.class);
    private final static ObjectMapper objectMapper = JsonMapper.builder().addModule(
            new SimpleModule().addDeserializer(CqlQuery.class, new CqlQueryDeserializer())).build();


    public static CqlQuery extract(String encodedCqlQuery, ConverterTemplate template) throws CqlExtractorException {
        String decodedCqlQuery = Base64Utils.decodeIfNecessary(encodedCqlQuery);
        logger.info("Extracting query: " + decodedCqlQuery);
        CqlQuery cqlQuery = parse(decodedCqlQuery);
        LibraryContentDataEditor.edit(cqlQuery.library(), template);
        return cqlQuery;
    }

    private static CqlQuery parse(String cqlQuery) throws CqlExtractorException {
        try {
            return objectMapper.readValue(cqlQuery, CqlQuery.class);
        } catch (JacksonException e) {
            throw new CqlExtractorException(e);
        }
    }


}
