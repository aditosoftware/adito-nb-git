package de.adito.git.impl.data;

import de.adito.git.api.data.IFileChanges;
import de.adito.git.api.data.ILineChange;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Object to store the LineChanges in {@link LineChangeImpl}
 *
 * @author m.kaspera 24.09.2018
 */
public class FileChangesImpl implements IFileChanges {

    private List<ILineChange> lineChanges;

    public FileChangesImpl(List<ILineChange> pLineChanges) {
        lineChanges = pLineChanges;
    }

    /**
     * @return List of {@link LineChangeImpl}
     */
    public List<ILineChange> getChanges() {
        return lineChanges;
    }

    @Nullable
    @Override
    public ILineChange getChange(int lineNumber) {
        if (lineChanges.size() > lineNumber)
            return lineChanges.get(lineNumber);
        else
            return null;
    }

    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();
        for (ILineChange lineChange : lineChanges) {
            returnString.append(lineChange.toString()).append("\n");
        }
        return returnString.toString();
    }
}
