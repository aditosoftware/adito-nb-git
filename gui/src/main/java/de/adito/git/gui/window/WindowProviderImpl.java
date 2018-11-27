package de.adito.git.gui.window;

import com.google.inject.Inject;
import de.adito.git.api.CommitHistoryTreeListItem;
import de.adito.git.api.IRepository;
import de.adito.git.api.IUserPreferences;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.tableModels.CommitHistoryTreeListTableModel;
import de.adito.git.gui.window.content.IWindowContentProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * A provider for all windows (not dialogs). This class only displays the windows
 *
 * @author a.arnold, 31.10.2018
 */
class WindowProviderImpl implements IWindowProvider {

    private final IWindowContentProvider factory;
    private final IUserPreferences userPreferences;

    @Inject
    public WindowProviderImpl(IWindowContentProvider pProvider, IUserPreferences pUserPreferences) {
        factory = pProvider;
        userPreferences = pUserPreferences;
    }

    @Override
    public void showBranchListWindow(Observable<Optional<IRepository>> pRepository) {
        _showInFrame(factory.createBranchListWindowContent(pRepository));
    }

    @Override
    public void showCommitHistoryWindow(Observable<Optional<IRepository>> pRepository, IBranch pBranch) {
        try {
            IRepository repo = pRepository.blockingFirst().orElseThrow(() -> new RuntimeException("No valid repository found"));
            List<ICommit> commits = repo.getCommits(pBranch, 0, userPreferences.getNumLoadAdditionalCHEntries());
            CommitHistoryTreeListTableModel tableModel = new CommitHistoryTreeListTableModel(repo.getCommitHistoryTreeList(commits, null));
            Runnable loadMoreCallBack = () -> {
                try {
                    tableModel.addData(
                            repo.getCommitHistoryTreeList(
                                    repo.getCommits(pBranch, tableModel.getRowCount(), userPreferences.getNumLoadAdditionalCHEntries()),
                                    (CommitHistoryTreeListItem) tableModel.getValueAt(tableModel.getRowCount() - 1, 0)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            _showInFrame(factory.createCommitHistoryWindowContent(pRepository, tableModel, loadMoreCallBack));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showFileCommitHistoryWindow(Observable<Optional<IRepository>> pRepository, File pFile) {
        try {
            IRepository repo = pRepository.blockingFirst().orElseThrow(() -> new RuntimeException("No valid repository found"));
            List<ICommit> commits = repo.getCommits(pFile, userPreferences.getNumLoadAdditionalCHEntries());
            CommitHistoryTreeListTableModel tableModel = new CommitHistoryTreeListTableModel(repo.getCommitHistoryTreeList(commits, null));
            Runnable loadMoreCallBack = () -> {
                try {
                    tableModel.addData(
                            repo.getCommitHistoryTreeList(
                                    repo.getCommits(pFile, tableModel.getRowCount(), userPreferences.getNumLoadAdditionalCHEntries()),
                                    (CommitHistoryTreeListItem) tableModel.getValueAt(tableModel.getRowCount() - 1, 0)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            _showInFrame(factory.createCommitHistoryWindowContent(pRepository, tableModel, loadMoreCallBack));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showStatusWindow(Observable<Optional<IRepository>> pRepository) {
        _showInFrame(factory.createStatusWindowContent(pRepository));
    }

    private void _showInFrame(JComponent pComponent) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(pComponent);
        frame.pack();
        frame.setVisible(true);
    }

}
