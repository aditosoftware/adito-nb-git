package de.adito.git.api.data;

import java.util.List;

/**
 * @author m.kaspera 25.09.2018
 */
public interface IFileChanges {

    List<ILineChange> getChanges();

    ILineChange getChange(int lineNumber);
}
