package de.adito.git.gui.window;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import io.reactivex.Observable;

import java.io.File;
import java.util.List;

/**
 * @author m.kaspera 29.10.2018
 */
public interface IWindowProvider {

    StatusWindow getStatusWindow(Observable<IRepository> pRepository);

    CommitHistoryWindow getCommitHistoryWindow(Observable<IRepository> pRepository, IBranch pBranch) throws Exception;

    CommitHistoryWindow getCommitHistoryWindow(Observable<IRepository> pRepository, File pFile) throws Exception;

    CommitHistoryWindow getCommitHistoryWindow(Observable<IRepository> pRepository, List<ICommit> pCommits);

    BranchListWindow getBranchListWindow(Observable<IRepository> pRepository);

}
