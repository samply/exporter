package de.samply.opal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.samply.exporter.ExporterConst;
import de.samply.opal.model.*;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OpalClientBodyFactory {
    private final static ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule());


    public static String createViewAndSerializeAsJson(Session session, ContainerTemplate containerTemplate) throws JsonProcessingException {
        return objectMapper.writeValueAsString(OpalClientBodyFactory.createView(session, containerTemplate));
    }

    public static String createCsvDatasourceBodyAndSerializeAsJson(Session session, ContainerTemplate containerTemplate, String data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(OpalClientBodyFactory.createCSVDatasourceBody(session, containerTemplate, data));
    }

    public static String createProjectBodyAndSerializeAsJson(String projectName, String database) throws JsonProcessingException {
        return objectMapper.writeValueAsString(OpalClientBodyFactory.createProjectBody(projectName, database));
    }

    public static String createPathBodyAndSerializeAsJson(Session session, ContainerTemplate template, String transientUid) throws JsonProcessingException {
        return objectMapper.writeValueAsString(OpalClientBodyFactory.createPathBody(session, template, transientUid));
    }

    public static View createView(Session session, ContainerTemplate containerTemplate) {
        View view = new View();
        view.setName(OpalUtils.fetchViewName(containerTemplate));
        view.setFrom(fetchViewFrom(session, containerTemplate));
        view.setMagmaView(createMagmaView(session, containerTemplate));
        return view;
    }

    public static CsvDatasource createCSVDatasourceBody(Session session, ContainerTemplate containerTemplate, String data) {
        CsvDatasource body = new CsvDatasource();
        CsvDatasource.Params params = new CsvDatasource.Params();
        params.setCharacterSet("UTF-8");
        params.setFirstRow(1);
        params.setQuote("\"");
        params.setSeparator(normalizeSeparator(session.getConverterTemplate().getCsvSeparator()));
        params.setDefaultValueType("text");
        params.setTables(List.of(createCSVDatasourceTable(session, containerTemplate, data)));
        body.setParams(params);
        return body;
    }

    public static CreateProjectBody createProjectBody(String projectName, String database) {
        CreateProjectBody body = new CreateProjectBody();
        body.setName(projectName);
        body.setTitle(projectName);
        body.setDescription("");
        body.setDatabase(database);
        body.setVcfStoreService(null);
        body.setExportFolder("");
        return body;
    }

    public static ImportPathBody createPathBody(Session session, ContainerTemplate containerTemplate, String transientUid) {
        ImportPathBody body = new ImportPathBody();
        body.setDestination(session.fetchProject());
        body.setTables(new String[]{transientUid + "." + containerTemplate.getOpalTable()});
        return body;
    }

    public static CsvDatasourceTable createCSVDatasourceTable(Session session, ContainerTemplate containerTemplate, String data) {
        CsvDatasourceTable table = new CsvDatasourceTable();
        table.setName(containerTemplate.getOpalTable());
        table.setData(data);
        table.setEntityType(containerTemplate.getOpalEntityType());
        table.setRefTable(session.fetchProject() + "." + containerTemplate.getOpalTable());
        return table;
    }

    private static List<String> fetchViewFrom(Session session, ContainerTemplate containerTemplate) {
        return Arrays.asList(session.fetchProject() + '.' + containerTemplate.getOpalTable());
    }

    private static MagmaView createMagmaView(Session session, ContainerTemplate containerTemplate) {
        MagmaView magmaView = new MagmaView();
        List<Variable> variables = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        containerTemplate.getAttributeTemplates().stream().filter(attributeTemplate -> !attributeTemplate.isPrimaryKey()).forEach(attributeTemplate -> variables.add(
                createVariable(session, containerTemplate, attributeTemplate, counter.getAndIncrement())));
        magmaView.setVariables(variables);
        return magmaView;
    }

    private static Variable createVariable(Session session, ContainerTemplate containerTemplate,
                                           AttributeTemplate attributeTemplate, int counter) {
        Variable variable = new Variable();
        variable.setIndex(counter);
        variable.setName(attributeTemplate.getCsvColumnName());
        variable.setValueType(attributeTemplate.getOpalValueType());
        variable.setEntityType(containerTemplate.getOpalEntityType());
        variable.setAttributes(fetchAttributes(session, containerTemplate, attributeTemplate));
        return variable;
    }

    private static List<Attribute> fetchAttributes(Session session,
                                                   ContainerTemplate containerTemplate, AttributeTemplate attributeTemplate) {
        List<Attribute> attributes = new ArrayList<>();

        Attribute attribute1 = new Attribute();
        attribute1.setName(ExporterConst.OPAL_ATTRIBUTE_DERIVED_FROM);
        attribute1.setNamespace(ExporterConst.OPAL_DEFAULT_NAMESPACE);
        attribute1.setValue(
                OpalUtils.createVariablePath(session, containerTemplate, attributeTemplate));
        attributes.add(attribute1);

        Attribute attribute2 = new Attribute();
        attribute2.setName(ExporterConst.OPAL_ATTRIBUTE_SCRIPT);
        attribute2.setValue(fetchScript(attributeTemplate));
        attributes.add(attribute2);

        return attributes;
    }

    private static String fetchScript(AttributeTemplate attributeTemplate) {
        return (attributeTemplate.getOpalScript() != null) ? attributeTemplate.getOpalScript()
                : fetchDefaultScript(attributeTemplate);
    }

    private static String fetchDefaultScript(AttributeTemplate attributeTemplate) {
        return "$('" + attributeTemplate.getCsvColumnName() + "')";
    }

    private static String normalizeSeparator(String separator) {
        return separator.replace("\t", "\\t");
    }
}
