package de.adito.git.nbm.topComponents;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.ITopComponentDisplayer;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.inject.Singleton;
import javax.swing.*;

/**
 * Show the NetBeans Components in {@link TopComponent}
 *
 * @author a.arnold, 23.10.2018
 */
@Singleton
public class NBTopComponentDisplayer implements ITopComponentDisplayer {

    @Override
    public void setComponent(JComponent jComponent) {
        TopComponent topComponent = new TopComponent();
        topComponent.add(jComponent);
        topComponent.setVisible(true);
    }

    /**
     * Open a new Topcomponent of {@link AllBranchTopComponent}
     * @param pRepository The repository for which the branches shoud be displayed
     */
    @Override
    public void showBranchWindow(Observable<IRepository> pRepository) {
        AllBranchTopComponent topComponent = new AllBranchTopComponent(pRepository, this);
        _openTCinEDT(topComponent, AllBranchTopComponent.MODE);
    }

    @Override
    public void showAllCommits(Observable<IRepository> repository, IBranch branch) throws Exception {
        AllCommitsTopComponent topComponent = null;
        try {
            topComponent = new AllCommitsTopComponent(repository, branch);
        } catch (Exception e) {
            throw  new Exception("failed to show all commits for: "+ branch.getName(), e);
        }
        _openTCinEDT(topComponent, AllCommitsTopComponent.MODE);
    }

    private static void _openTCinEDT(@NotNull TopComponent pComponent, @NotNull String pMode)
    {
        SwingUtilities.invokeLater(() -> {
            pComponent.open();
            WindowManager.getDefault().findMode(pMode).dockInto(pComponent);
            pComponent.requestActive();
        });
    }


}
