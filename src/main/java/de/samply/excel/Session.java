package de.samply.excel;

import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateUtils;
import de.samply.template.token.TokenContext;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Session {


    private final ConverterTemplateUtils converterTemplateUtils;
    private final String writeDirectory;
    private final Integer workbookWindow;
    private final List<Workbook> workbooks = new ArrayList<>();
    private final TokenContext tokenContext;

    private Path basicExcelPath;

    private boolean isExcelFileAlreadyCreated = false;

    public Session(ConverterTemplateUtils converterTemplateUtils, String writeDirectory,
                   Integer workbookWindow, TokenContext tokenContext) {
        this.converterTemplateUtils = converterTemplateUtils;
        this.writeDirectory = writeDirectory;
        this.workbookWindow = workbookWindow;
        this.tokenContext = tokenContext;
    }

    public Workbook fetchWorkbook() {
        return (workbooks.isEmpty()) ? addNewWorkbook() : workbooks.get(workbooks.size() - 1);
    }

    public Workbook addNewWorkbook() {
        isExcelFileAlreadyCreated = false;
        Workbook result = new SXSSFWorkbook(workbookWindow);
        workbooks.add(result);
        return result;
    }

    public Path getExcelPath(ConverterTemplate converterTemplate) {
        return getExcelPath(converterTemplate, null);
    }

    public Path getExcelPathOfLastWorkbook(ConverterTemplate converterTemplate) {
        return (workbooks.size() > 0) ? getExcelPath(converterTemplate, workbooks.size()) : getExcelPath(converterTemplate);
    }

    public Path getExcelPath(ConverterTemplate converterTemplate, Integer counter) {
        if (basicExcelPath == null) {
            String filename = converterTemplateUtils.replaceTokens(converterTemplate.getExcelFilename(), tokenContext);
            basicExcelPath = Paths.get(writeDirectory).resolve(filename);
        }
        return (counter != null) ? basicExcelPath.getParent().resolve(createFilenameWithCounter(basicExcelPath, counter)) : basicExcelPath;
    }

    private String createFilenameWithCounter(Path path, int counter) {
        String filename = path.getFileName().toString();
        int index = filename.lastIndexOf(".");
        if (counter > 1) {
            if (index > 0) {
                String extension = filename.substring(index);
                filename = filename.substring(0, index);
                filename = filename + '_' + counter + extension;
            } else {
                filename += counter;
            }
        }
        return filename;
    }

    public boolean isExcelFileAlreadyCreated() {
        return isExcelFileAlreadyCreated;
    }

    public void setExcelFileAlreadyCreated(boolean excelFileAlreadyCreated) {
        isExcelFileAlreadyCreated = excelFileAlreadyCreated;
    }

    public List<Workbook> getWorkbooks() {
        return workbooks;
    }

}
