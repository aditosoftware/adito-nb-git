package de.adito.git.api.data;

/**
 * @author m.kaspera 24.09.2018
 */
public class LineChange {

    private final EChangeType changeType;
    private final String lineContent;

    public LineChange(EChangeType pChangeType, String pLineContent){
        changeType = pChangeType;
        lineContent = pLineContent;
    }

    public EChangeType getChangeType() {
        return changeType;
    }

    public String getLineContent() {
        return lineContent;
    }
}
