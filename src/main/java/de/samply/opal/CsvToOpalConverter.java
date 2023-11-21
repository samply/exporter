package de.samply.opal;

import de.samply.converter.Format;
import de.samply.converter.TargetConverterImpl;
import de.samply.exporter.ExporterConst;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateUtils;
import de.samply.template.token.TokenContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import reactor.core.publisher.Flux;

import java.nio.file.Path;

public class CsvToOpalConverter extends TargetConverterImpl<Path, Path, Session> implements
        ApplicationContextAware {

    private final static Logger logger = BufferedLoggerFactory.getLogger(CsvToOpalConverter.class);
    private String targetId;
    private OpalEngine opalEngine;
    private ApplicationContext applicationContext;

    public CsvToOpalConverter(String targetId, OpalServer opalServer) {
        this.targetId = targetId;
        this.opalEngine = new OpalEngine(opalServer);
    }

    @Override
    public Format getInputFormat() {
        return Format.CSV;
    }

    @Override
    public Format getOutputFormat() {
        return Format.OPAL;
    }

    @Override
    protected Flux<Path> convert(Path input, ConverterTemplate template, Session session) {
        try {
            opalEngine.sendPathToOpal(input, session);
        } catch (OpalEngineException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return Flux.just(input);
    }

    @Override
    protected Session initializeSession(ConverterTemplate template, TokenContext tokenContext) {
        Session session = new Session(template, fetchConverterTemplateUtils(), fetchTimeoutInSeconds(), fetchMaxNumberOfRetries(), tokenContext);
        createProjectIfNotExists(session);
        return session;
    }

    private void createProjectIfNotExists(Session session) {
        try {
            opalEngine.createProjectIfNotExists(session);
        } catch (OpalEngineException e) {
            throw new RuntimeException(e);
        }
    }

    private ConverterTemplateUtils fetchConverterTemplateUtils() {
        return (applicationContext != null) ? applicationContext.getBean(ConverterTemplateUtils.class)
                : null;
    }

    private Integer fetchMaxNumberOfRetries() {
        return fetchIntegerProperty(ExporterConst.MAX_NUMBER_OF_RETRIES, ExporterConst.DEFAULT_MAX_NUMBER_OF_RETRIES);
    }

    private Integer fetchTimeoutInSeconds() {
        return fetchIntegerProperty(ExporterConst.TIMEOUT_IN_SECONDS, ExporterConst.DEFAULT_TIMEOUT_IN_SECONDS);
    }

    private Integer fetchIntegerProperty(String property, int defaultValue) {
        if (applicationContext != null) {
            String value = applicationContext.getEnvironment().getProperty(property);
            if (value != null) {
                return Integer.valueOf(value);
            }
        }
        return defaultValue;
    }

    @Override
    public String getTargetId() {
        return targetId;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
