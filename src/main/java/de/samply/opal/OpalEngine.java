package de.samply.opal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.samply.exporter.ExporterConst;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.opal.model.View;
import de.samply.template.ContainerTemplate;
import org.apache.commons.collections4.CollectionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class OpalEngine {

    private final static Logger logger = BufferedLoggerFactory.getLogger(OpalEngine.class);
    private OpalServer opalServer;
    private ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule());

    public OpalEngine(OpalServer opalServer) {
        this.opalServer = opalServer;
    }

    public void sendPathToOpal(Path path, Session session) throws OpalEngineException {
        ContainerTemplate containerTemplate = session.getContainerTemplate(
                path.getFileName().toString());
        if (containerTemplate != null) {
            //TODO: Check status
            uploadPath(path, session);
            ProcessInfo importProcessInfo = importPath(path, session, containerTemplate);
            waitUntilTaskIsFinished(fetchTaskId(importProcessInfo));
            createView(session, containerTemplate);
            deletePath(path, session);
        }
    }

    public void createProjectIfNotExists(Session session) throws OpalEngineException {
        if (!existsProject(session)) {
            createProject(session);
            createProjectPermissions(session);
        }
    }

    private boolean existsProject(Session session) throws OpalEngineException {
        String[] arguments = {ExporterConst.OPAL_PROJECT_NAME, session.fetchProject()};
        return executeOpalCommands(ExporterConst.OPAL_PROJECT_OP, arguments).status == 0;
    }

    private List<String> addOpalCredentials(List<String> commands) {
        String[] tempCommands = {ExporterConst.OPAL_URL, opalServer.getUrl(), ExporterConst.OPAL_USER,
                opalServer.getUser(), ExporterConst.OPAL_PASSWORD, opalServer.getPassword()};
        CollectionUtils.addAll(commands, tempCommands);
        return commands;
    }


    private ProcessInfo createProject(Session session) throws OpalEngineException {
        String[] arguments = {ExporterConst.OPAL_PROJECT_ADD, ExporterConst.OPAL_PROJECT_NAME,
                session.fetchProject(), ExporterConst.OPAL_PROJECT_DATABASE, opalServer.getDatabase()};
        return executeOpalCommands(ExporterConst.OPAL_PROJECT_OP, arguments);
    }

    private List<ProcessInfo> createProjectPermissions(Session session) throws OpalEngineException {
        List<ProcessInfo> results = new ArrayList<>();
        String subjects = session.getConverterTemplate().getOpalPermissionSubjects();
        if (subjects != null && subjects.length() > 0) {
            for (String subject : subjects.trim()
                    .split(ExporterConst.OPAL_PERMISSION_SUBJECT_SEPARATOR)) {
                String[] arguments = {
                        ExporterConst.OPAL_PERMISSION_USER_TYPE,
                        session.getConverterTemplate().getOpalPermissionType().toString(),
                        ExporterConst.OPAL_PERMISSION_SUBJECT, subject,
                        ExporterConst.OPAL_PERMISSION,
                        session.getConverterTemplate().getOpalPermission().toString(),
                        ExporterConst.OPAL_PERMISSION_PROJECT, session.fetchProject(),
                        ExporterConst.OPAL_PERMISSION_ADD
                };
                results.add(executeOpalCommands(ExporterConst.OPAL_PERMISSION_OP, arguments));
            }
        }
        return results;
    }

    private ProcessInfo uploadPath(Path path, Session session) throws OpalEngineException {
        String[] arguments = {ExporterConst.OPAL_FILE_UPLOAD, path.toString(),
                session.fetchOpalProjectDirectory(opalServer.getFilesDirectory())};
        return executeOpalCommands(ExporterConst.OPAL_FILE_OP, arguments);
    }

    private ProcessInfo deletePath(Path path, Session session) throws OpalEngineException {
        String[] arguments = {ExporterConst.OPAL_FILE_DELETE,
                session.fetchOpalProjectDirectoryPath(opalServer.getFilesDirectory(), path),
                ExporterConst.OPAL_FILE_FORCE};
        return executeOpalCommands(ExporterConst.OPAL_FILE_OP, arguments);
    }

    private ProcessInfo importPath(Path path, Session session, ContainerTemplate template)
            throws OpalEngineException {
        String[] arguments = {ExporterConst.OPAL_IMPORT_CSV_DESTINATION,
                session.fetchProject(), ExporterConst.OPAL_IMPORT_CSV_PATH,
                session.fetchOpalProjectDirectoryPath(opalServer.getFilesDirectory(), path),
                ExporterConst.OPAL_IMPORT_CSV_TABLES, template.getOpalTable(),
                ExporterConst.OPAL_IMPORT_CSV_SEPARATOR,
                normalizeSepartor(session.getConverterTemplate().getCsvSeparator()),
                ExporterConst.OPAL_IMPORT_CSV_TYPE, template.getOpalEntityType()};
        arguments = addIdentifiersToArguments(arguments, template);
        return executeOpalCommands(ExporterConst.OPAL_IMPORT_CSV_OP, arguments);
    }

    private String[] addIdentifiersToArguments(String[] arguments,
                                               ContainerTemplate containerTemplate) {
        List<String> result = new ArrayList<>();
        result.addAll(Arrays.asList(arguments));
        result.addAll(fetchIdentifiers(containerTemplate));
        return result.toArray(result.toArray(new String[0]));
    }

    private List<String> fetchIdentifiers(ContainerTemplate containerTemplate) {
        List<String> identifiers = new ArrayList<>();
        containerTemplate.getAttributeTemplates().forEach(attributeTemplate -> {
            if (attributeTemplate.isPrimaryKey()) {
                identifiers.add(attributeTemplate.getCsvColumnName());
            }
        });
        if (identifiers.size() > 0) {
            identifiers.add(0, ExporterConst.OPAL_IMPORT_CSV_IDENTIFIERS);
        }
        return identifiers;
    }

    private ProcessInfo waitUntilTaskIsFinished(String taskId) throws OpalEngineException {
        if (taskId != null) {
            String[] arguments = {ExporterConst.OPAL_TASK_OP_ID, taskId, ExporterConst.OPAL_TASK_OP_WAIT};
            return executeOpalCommands(ExporterConst.OPAL_TASK_OP, arguments);
        } else {
            throw new OpalEngineException("Task " + taskId + " not found");
        }
    }

  /*
  private void formatTable(Session session, ContainerTemplate containerTemplate) {
    for (AttributeTemplate attributeTemplate : containerTemplate.getAttributeTemplates()) {
      formatVariable(session, containerTemplate, attributeTemplate);
    }
  }

  private void formatVariable(Session session, ContainerTemplate containerTemplate,
      AttributeTemplate attributeTemplate) {
    try {
      formatVariableWithoutExceptionHandling(session, containerTemplate, attributeTemplate);
    } catch (OpalEngineException | JsonProcessingException e) {
      logger.error(ExceptionUtils.getFullStackTrace(e));
    }
  }

  private void formatVariableWithoutExceptionHandling(Session session,
      ContainerTemplate containerTemplate, AttributeTemplate attributeTemplate)
      throws OpalEngineException, JsonProcessingException {
    if (attributeTemplate.getOpalValueType() != null && !attributeTemplate.getOpalValueType()
        .equals(ExporterConst.OPAL_DEFAULT_VALUE_TYPE)) {
      JsonNode variableInfo = fetchVariableInfo(session, containerTemplate, attributeTemplate);
      ((ObjectNode) variableInfo).set(ExporterConst.OPAL_VALUE_TYPE,
          new TextNode(attributeTemplate.getOpalValueType()));
      String sVariableInfo = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(variableInfo);
      modifyVariable(session, containerTemplate, attributeTemplate, sVariableInfo);
    }
  }

  private ProcessInfo modifyVariable(Session session,
      ContainerTemplate containerTemplate,
      AttributeTemplate attributeTemplate, String variableInfo) throws OpalEngineException {
    String subOperation = createVariablePath(session.fetchProject(),
        containerTemplate.getOpalTable(), attributeTemplate.getCsvColumnName());
    String[] arguments = {ExporterConst.OPAL_REST_METHOD, ExporterConst.OPAL_REST_METHOD_PUT,
        ExporterConst.OPAL_REST_CONTENT_TYPE, ExporterConst.OPAL_REST_CONTENT_TYPE_JSON};
    return executeOpalCommands(ExporterConst.OPAL_REST_OP, subOperation, arguments, variableInfo);
  }

  private JsonNode fetchVariableInfo(Session session, ContainerTemplate containerTemplate,
      AttributeTemplate attributeTemplate) throws OpalEngineException, JsonProcessingException {
    String subOperation = createVariablePath(session.fetchProject(),
        containerTemplate.getOpalTable(), attributeTemplate.getCsvColumnName());
    String[] arguments = {ExporterConst.OPAL_REST_METHOD, ExporterConst.OPAL_REST_METHOD_GET};
    ProcessInfo processInfo = executeOpalCommands(ExporterConst.OPAL_REST_OP, subOperation,
        arguments);
    return objectMapper.readTree(processInfo.output);
  }

  private String createVariablePath(String project, String table, String variable) {
    return (ExporterConst.OPAL_PATH_PROJECT + '/' + project + ExporterConst.OPAL_PATH_TABLE + '/'
        + table + ExporterConst.OPAL_PATH_VARIABLE + '/' + variable).replace(" ", "%20");
  }

  private ProcessInfo fetchViewWithinMaxNumberOfRetries(Session session,
      ContainerTemplate containerTemplate)
      throws OpalEngineException {
    return fetchProcessInfoWithinMaxNumberOfRetries(session,
        () -> fetchView(session, containerTemplate), processInfo -> processInfo.status == 0,
        "View could not be created");
  }
   */

    private ProcessInfo fetchProcessInfoWithinMaxNumberOfRetries(Session session,
                                                                 ProcessInfoSupplier supplier, ProcessInfoCondition condition, String errorMessage)
            throws OpalEngineException {
        ProcessInfo result = null;
        int numberOfRetries = session.getMaxNumberOfRetries();
        boolean processInfoExecutedCorrectly = false;
        while (!processInfoExecutedCorrectly && numberOfRetries > 0) {
            result = supplier.get();
            if (!condition.isFullfilled(result)) {
                numberOfRetries--;
                sleep(session.getTimeoutInSeconds());
            } else {
                processInfoExecutedCorrectly = true;
            }
        }
        if (!processInfoExecutedCorrectly) {
            throw new OpalEngineException(errorMessage);
        }
        return result;
    }

    private interface ProcessInfoSupplier {

        ProcessInfo get() throws OpalEngineException;
    }

    private interface ProcessInfoCondition {

        boolean isFullfilled(ProcessInfo processInfo) throws OpalEngineException;
    }

    private void sleep(int timeoutInSeconds) throws OpalEngineException {
        try {
            Thread.sleep(timeoutInSeconds * 1000);
        } catch (InterruptedException e) {
            throw new OpalEngineException(e);
        }
    }

    /*
    private ProcessInfo fetchTableWithinMaxNumberOfRetries(Session session,
        ContainerTemplate containerTemplate)
        throws OpalEngineException {
      return fetchProcessInfoWithinMaxNumberOfRetries(session,
          () -> fetchTable(session, containerTemplate), processInfo -> isTableReady(processInfo),
          "Error reading table");
    }
  */
    private boolean isTableReady(ProcessInfo processInfo) throws OpalEngineException {
        boolean result = processInfo.status == 0 && processInfo.output != null;
        if (result) {
            String status = fetchStatus(processInfo);
            result = status != null && status.equals(ExporterConst.OPAL_STATUS_READY);
        }
        return result;
    }

    private String fetchStatus(ProcessInfo processInfo) throws OpalEngineException {
        return fetchVariable(processInfo, ExporterConst.OPAL_STATUS);
    }

    private String fetchTaskId(ProcessInfo processInfo) throws OpalEngineException {
        return fetchVariable(processInfo, ExporterConst.OPAL_TASK_ID);
    }

    private String fetchVariable(ProcessInfo processInfo, String variable)
            throws OpalEngineException {
        try {
            return fetchVariableWithoutExceptionHandling(processInfo, variable);
        } catch (JsonProcessingException e) {
            throw new OpalEngineException(e);
        }
    }

    private String fetchVariableWithoutExceptionHandling(ProcessInfo processInfo, String variable)
            throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(processInfo.output);
        JsonNode jsonNode2 = jsonNode.get(variable);
        return (jsonNode2 != null) ? jsonNode2.toString().replace("\"", "") : null;
    }

    /*
    private ProcessInfo fetchTable(Session session, ContainerTemplate containerTemplate)
        throws OpalEngineException {
      String suboperation = OpalUtils.createTablePath(session, containerTemplate);
      String[] arguments = {ExporterConst.OPAL_REST_METHOD, ExporterConst.OPAL_REST_METHOD_GET};
      return executeOpalCommands(ExporterConst.OPAL_REST_OP, suboperation, arguments);
    }

    private ProcessInfo fetchView(Session session, ContainerTemplate containerTemplate)
        throws OpalEngineException {
      String suboperation = OpalUtils.createViewPath(session, containerTemplate);
      String[] arguments = {ExporterConst.OPAL_REST_METHOD, ExporterConst.OPAL_REST_METHOD_GET};
      return executeOpalCommands(ExporterConst.OPAL_REST_OP, suboperation, arguments);
    }
  */
    private ProcessInfo createView(Session session, ContainerTemplate containerTemplate)
            throws OpalEngineException {
        try {
            return createViewWithoutExceptionHandling(session, containerTemplate);
        } catch (JsonProcessingException e) {
            throw new OpalEngineException(e);
        }
    }

    private ProcessInfo createViewWithoutExceptionHandling(Session session,
                                                           ContainerTemplate containerTemplate)
            throws JsonProcessingException, OpalEngineException {
        View view = ViewFactory.createView(session, containerTemplate);
        String sView = objectMapper.writeValueAsString(view);
        String suboperation = OpalUtils.createViewsPath(session);
        String[] arguments = {ExporterConst.OPAL_REST_METHOD, ExporterConst.OPAL_REST_METHOD_POST,
                ExporterConst.OPAL_REST_CONTENT_TYPE, ExporterConst.OPAL_REST_CONTENT_TYPE_JSON};
        return executeOpalCommands(ExporterConst.OPAL_REST_OP, suboperation, arguments, sView);
    }

    private String normalizeSepartor(String separator) {
        return separator.replace("\t", "\\t");
    }

    private ProcessInfo executeOpalCommands(String operation, String[] arguments)
            throws OpalEngineException {
        return executeOpalCommands(operation, arguments, null);
    }

    private ProcessInfo executeOpalCommands(String operation, String[] arguments, String input)
            throws OpalEngineException {
        return executeOpalCommands(operation, null, arguments, input);
    }

    private ProcessInfo executeOpalCommands(String operation, String subOperation,
                                            String[] arguments)
            throws OpalEngineException {
        return executeOpalCommands(operation, subOperation, arguments, null);
    }

    private ProcessInfo executeOpalCommands(String operation, String subOperation,
                                            String[] arguments, String input)
            throws OpalEngineException {
        List<String> commands = new ArrayList<>();
        commands.add(ExporterConst.OPAL_CMD);
        commands.add(operation);
        if (subOperation != null) {
            commands.add(subOperation);
        }
        commands = addOpalCredentials(commands);
        CollectionUtils.addAll(commands, arguments);
        return executeCommands(commands, input);
    }

    private ProcessInfo executeCommands(List<String> commands, String input)
            throws OpalEngineException {
        try {
            return executeCommandsWithoutExceptionHandling(commands, input);
        } catch (IOException | InterruptedException e) {
            throw new OpalEngineException(e);
        }
    }

    private ProcessInfo executeCommandsWithoutExceptionHandling(List<String> commands, String input)
            throws IOException, InterruptedException {
        logger.info(fetchCommand(commands));
        if (input != null) {
            logger.info(input);
        }
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commands);
        Process process = processBuilder.start();
        if (input != null) {
            OutputStream stdin = process.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(stdin));
            bufferedWriter.write(input);
            bufferedWriter.flush();
            bufferedWriter.close();
        }
        String output = logProcess(process);
        int status = process.waitFor();
        if (status > 0) {
            logProcessError(process);
        }
        return new ProcessInfo(process, status, output);

    }

    private record ProcessInfo(Process process, int status, String output) {

    }

    private String logProcess(Process process) throws IOException {
        return logProcess(process.getInputStream(), logger::info);
    }

    private String logProcessError(Process process) throws IOException {
        return logProcess(process.getErrorStream(), logger::error);
    }

    private String logProcess(InputStream inputStream, Consumer<String> logger) throws IOException {
        try (var reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                logger.accept(line);
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
    }

    private String fetchCommand(List<String> commands) {
        StringBuilder stringBuilder = new StringBuilder();
        AtomicBoolean printNext = new AtomicBoolean(true);
        commands.forEach(command -> {
            if (printNext.get()) {
                stringBuilder.append(command);
            } else {
                stringBuilder.append(ExporterConst.PASSWORD_REPLACEMENT);
            }
            stringBuilder.append(' ');
            printNext.set(!isPassword(command));
        });
        return stringBuilder.toString();
    }

    private boolean isPassword(String element) {
        boolean result = element != null && element.length() > 0;
        if (result) {
            result = Arrays.stream(ExporterConst.PASSWORD_BLACKLIST)
                    .anyMatch(element.toLowerCase()::contains);
        }
        return result;
    }

}
