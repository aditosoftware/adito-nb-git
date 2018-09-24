package de.adito.git.api.data;

import java.util.List;

/**
 * @author m.kaspera 24.09.2018
 */
public class FileChangeObj {

    private List<LineChange> lineChanges;

    public FileChangeObj(List<LineChange> pLineChanges){
        lineChanges = pLineChanges;
    }

    public List<LineChange> getLineChanges() {
        return lineChanges;
    }

}
