package de.samply.explorer;

import org.springframework.lang.NonNull;

public class PivotIdentifier {
    private String filename;
    private String column;

    public PivotIdentifier(@NonNull String fileColumn) throws PivotIdentifierException {
        int index = fileColumn.lastIndexOf(".");
        if (index < 0){
            throw new PivotIdentifierException("Missing '.' between file and column");
        }
        filename = fileColumn.substring(0, index);
        column = fileColumn.substring(index +1);
    }

    public String getFilename() {
        return filename;
    }

    public String getColumn() {
        return column;
    }

}
