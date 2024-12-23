package de.samply.opal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.samply.exporter.ExporterConst;
import de.samply.opal.model.Attribute;
import de.samply.opal.model.MagmaView;
import de.samply.opal.model.Variable;
import de.samply.opal.model.View;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewFactory {

    private final static ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule());


    public static String createViewAndSerializeAsJson(Session session, ContainerTemplate containerTemplate) throws JsonProcessingException {
        return objectMapper.writeValueAsString(ViewFactory.createView(session, containerTemplate));
    }

    public static View createView(Session session, ContainerTemplate containerTemplate) {
        View view = new View();
        view.setName(OpalUtils.fetchViewName(containerTemplate));
        view.setFrom(fetchViewFrom(session, containerTemplate));
        view.setMagmaView(createMagmaView(session, containerTemplate));
        return view;
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

}
