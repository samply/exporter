package de.samply.opal;

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
    containerTemplate.getAttributeTemplates().forEach(attributeTemplate -> variables.add(
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
    variable.setAttributes(attributeTemplate.isPrimaryKey() ? fetchIdAttributes()
        : fetchAttributes(session, containerTemplate, attributeTemplate));
    return variable;
  }

  private static List<Attribute> fetchIdAttributes() {
    Attribute attribute = new Attribute();
    attribute.setName(ExporterConst.OPAL_ATTRIBUTE_SCRIPT);
    attribute.setValue(ExporterConst.OPAL_ATTRIBUTE_SCRIPT_ID);
    return Arrays.asList(attribute);
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
    return "${'" + attributeTemplate.getCsvColumnName() + "'}";
  }

}
