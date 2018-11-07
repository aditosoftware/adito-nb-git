package de.adito.git.gui.window.content;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import io.reactivex.Observable;

import javax.swing.*;
import java.util.List;

/**
 * @author a.arnold, 31.10.2018
 */
public interface IWindowContentProvider {

    JComponent createStatusWindowContent(Observable<IRepository> pRepository);

    JComponent createBranchListWindowContent(Observable<IRepository> pRepository);

    JComponent createCommitHistoryWindowContent(Observable<IRepository> pRepository, List<ICommit> pCommits);

}
