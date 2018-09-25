package de.adito.git.api.data;

/**
 * Stores the pair of {@link EChangeType} and the content of the line as String
 * With a list of these a diff of a File can be represented
 *
 * @author m.kaspera 24.09.2018
 */
public class LineChangeImpl implements ILineChange{

    private final EChangeType changeType;
    private final String lineContent;

    public LineChangeImpl(EChangeType pChangeType, String pLineContent){
        changeType = pChangeType;
        lineContent = pLineContent;
    }

    public EChangeType getType() {
        return changeType;
    }

    public String getLineContent() {
        return lineContent;
    }

    @Override
    public String toString() {
        return (changeType == EChangeType.ADD ? "+" : "-") + lineContent;
    }
}
