package de.adito.git.nbm.window;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * A provider for all Windows in NetBeans
 *
 * @author a.arnold, 31.10.2018
 */
class WindowProviderNBImpl implements IWindowProvider {

    private final ITopComponentFactory topComponentFactory;

    @Inject
    public WindowProviderNBImpl(ITopComponentFactory pTopComponentFactory) {
        topComponentFactory = pTopComponentFactory;
    }

    @Override
    public void showBranchListWindow(Observable<IRepository> pRepository) {
        _openTCinEDT(topComponentFactory.createAllBranchTopComponent(pRepository));
    }

    @Override
    public void showCommitHistoryWindow(Observable<IRepository> pRepository, IBranch pBranch) {
        try {
            List<ICommit> commits = pRepository.blockingFirst().getCommits(pBranch);
            _openTCinEDT(topComponentFactory.createCommitHistoryTopComponent(pRepository, commits, pBranch.getSimpleName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showCommitHistoryWindow(Observable<IRepository> pRepository, File pFile) {
        try {
            List<ICommit> commits = pRepository.blockingFirst().getCommits(pFile);
            _openTCinEDT(topComponentFactory.createCommitHistoryTopComponent(pRepository, commits, pFile.getAbsolutePath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showStatusWindow(Observable<IRepository> pRepository) {
        throw new RuntimeException("de.adito.git.nbm.window.WindowProviderNBImpl.showStatusWindow"); //todo
    }

    /**
     * A helper class to open TopComponents in EDT
     * @param pComponent
     */
    private static void _openTCinEDT(@NotNull AbstractRepositoryTopComponent pComponent) {
        SwingUtilities.invokeLater(() -> {
            pComponent.open();
            WindowManager.getDefault().findMode(pComponent.getInitialMode()).dockInto(pComponent);
            pComponent.requestActive();
        });
    }
}
