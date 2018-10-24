package de.adito.git.nbm.topComponents;

import de.adito.git.api.IRepository;
import de.adito.git.gui.BranchListWindow;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;

/**
 * A {@link TopComponent} that show all branches of one repository
 *
 * @author a.arnold, 23.10.2018
 */

@TopComponent.Description(preferredID = "AllBranchTopComponent", persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
class AllBranchTopComponent extends TopComponent {
    private final Disposable displayNameDisposable;

    /**
     * @param pRepository The repository of which all branches should be shown
     */
    AllBranchTopComponent(Observable<IRepository> pRepository) {
        setLayout(new BorderLayout());
        add(new BranchListWindow(pRepository), BorderLayout.CENTER);

        //Set the displayname in the TopComponent of NetBeans.
        displayNameDisposable = pRepository.subscribe(pRepo -> SwingUtilities.invokeLater(() -> {
            setDisplayName("Git Branches - " + pRepo.getDirectory()); //todo I18N
        }));
    }

    @Override
    protected void componentClosed() {
        if (!displayNameDisposable.isDisposed())
            displayNameDisposable.dispose();
    }
}
