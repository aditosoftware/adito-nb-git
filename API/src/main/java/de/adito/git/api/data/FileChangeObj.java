package de.adito.git.api.data;

import java.util.List;

/**
 * Object to store the LineChanges in {@link de.adito.git.api.data.LineChange}
 *
 * @author m.kaspera 24.09.2018
 */
public class FileChangeObj {

    private List<LineChange> lineChanges;

    public FileChangeObj(List<LineChange> pLineChanges){
        lineChanges = pLineChanges;
    }

    /**
     *
     * @return List of {@link de.adito.git.api.data.LineChange}
     */
    public List<LineChange> getLineChanges() {
        return lineChanges;
    }

}
