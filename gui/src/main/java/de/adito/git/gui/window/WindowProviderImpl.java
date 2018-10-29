package de.adito.git.gui.window;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import io.reactivex.Observable;

import java.io.File;
import java.util.List;

/**
 * @author m.kaspera 29.10.2018
 */
class WindowProviderImpl implements IWindowProvider {

    private final IWindowFactory windowFactory;

    @Inject
    WindowProviderImpl(IWindowFactory pWindowFactory) {
        windowFactory = pWindowFactory;
    }

    @Override
    public StatusWindow getStatusWindow(Observable<IRepository> pRepository) {
        return windowFactory.createStatusWindow(pRepository);
    }

    @Override
    public CommitHistoryWindow getCommitHistoryWindow(Observable<IRepository> pRepository, IBranch pBranch) throws Exception {
        return new CommitHistoryWindow(pRepository, pBranch);
    }

    @Override
    public CommitHistoryWindow getCommitHistoryWindow(Observable<IRepository> pRepository, File pFile) throws Exception {
        return new CommitHistoryWindow(pRepository, pFile);
    }

    @Override
    public CommitHistoryWindow getCommitHistoryWindow(Observable<IRepository> pRepository, List<ICommit> pCommits) {
        return new CommitHistoryWindow(pRepository, pCommits);
    }

    @Override
    public BranchListWindow getBranchListWindow(Observable<IRepository> pRepository) {
        return windowFactory.createBranchListWindow(pRepository);
    }
}
