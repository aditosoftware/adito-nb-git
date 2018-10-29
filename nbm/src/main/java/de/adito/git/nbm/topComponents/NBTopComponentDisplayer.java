package de.adito.git.nbm.topComponents;

import de.adito.git.api.IRepository;
import de.adito.git.gui.ITopComponentDisplayer;
import io.reactivex.Observable;
import org.openide.windows.TopComponent;

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
     * Open a new TopComponent of {@link AllBranchTopComponent}
     * @param pRepository The repository for which the branches should be displayed
     */
    @Override
    public void showBranchWindow(Observable<IRepository> pRepository) {
        AllBranchTopComponent topComponent = new AllBranchTopComponent(pRepository);
        SwingUtilities.invokeLater(() -> {
            topComponent.open();
            topComponent.requestActive();
        });
    }


}
