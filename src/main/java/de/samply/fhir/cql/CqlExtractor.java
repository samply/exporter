package de.samply.fhir.cql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.template.ConverterTemplate;
import de.samply.utils.Base64Utils;

public class CqlExtractor {

    private final static Logger logger = BufferedLoggerFactory.getLogger(CqlExtractor.class);
    private final static ObjectMapper objectMapper = new ObjectMapper().registerModule(
            new SimpleModule().addDeserializer(CqlQuery.class, new CqlQueryDeserializer()));


    public static CqlQuery extract(String encodedCqlQuery, ConverterTemplate template) throws CqlExtractorException {
        String decodedCqlQuery = Base64Utils.decode(encodedCqlQuery);
        logger.info("Extracting query: " + decodedCqlQuery);
        CqlQuery cqlQuery = parse(decodedCqlQuery);
        LibraryContentDataEditor.edit(cqlQuery.library(), template);
        return cqlQuery;
    }

    private static CqlQuery parse(String cqlQuery) throws CqlExtractorException {
        try {
            return objectMapper.readValue(cqlQuery, CqlQuery.class);
        } catch (JsonProcessingException e) {
            throw new CqlExtractorException(e);
        }
    }


}
