package de.samply.opal;

import de.samply.exporter.ExporterConst;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;

public class OpalUtils {

  public static String createVariablePath(Session session, ContainerTemplate containerTemplate,
      AttributeTemplate attributeTemplate) {
    return createVariablePath(session.fetchProject(), containerTemplate.getOpalTable(),
        attributeTemplate.getCsvColumnName());
  }

  public static String createVariablePath(String project, String table, String variable) {
    return (ExporterConst.OPAL_PATH_PROJECT + '/' + project + ExporterConst.OPAL_PATH_TABLE + '/'
        + table + ExporterConst.OPAL_PATH_VARIABLE + '/' + variable).replace(" ", "%20");
  }

  public static String createViewsPath(Session session) {
    return ExporterConst.OPAL_PATH_PROJECT + '/' + session.fetchProject()
        + ExporterConst.OPAL_PATH_VIEWS;
  }

  public static String createViewPath(Session session, ContainerTemplate containerTemplate) {
    return ExporterConst.OPAL_PATH_PROJECT + '/' + session.fetchProject()
        + ExporterConst.OPAL_PATH_VIEW + '/' + fetchViewName(containerTemplate);
  }

  public static String createTablePath(Session session, ContainerTemplate containerTemplate) {
    return ExporterConst.OPAL_PATH_PROJECT + '/' + session.fetchProject()
        + ExporterConst.OPAL_PATH_TABLE + '/' + containerTemplate.getOpalTable();
  }

  public static String fetchViewName(ContainerTemplate containerTemplate) {
    return containerTemplate.getOpalTable() + ExporterConst.OPAL_VIEW_SUFFIX;
  }


}
