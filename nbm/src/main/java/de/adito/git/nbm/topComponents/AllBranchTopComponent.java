package de.adito.git.nbm.topComponents;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.gui.BranchListWindow;
import de.adito.git.gui.ITopComponentDisplayer;
import de.adito.git.gui.guice.AditoGitModule;
import de.adito.git.gui.window.IWindowProvider;
import de.adito.git.nbm.Guice.AditoNbmModule;
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
class AllBranchTopComponent extends AbstractRepositoryTopComponent {
    static final String MODE = "output";

    private Disposable displayNameDisposable;
    private IWindowProvider windowProvider;

    AllBranchTopComponent() {
        Injector injector = Guice.createInjector(new AditoGitModule(), new AditoNbmModule());
        windowProvider = injector.getInstance(IWindowProvider.class);
    }

    /**
     * @param pRepository The repository of which all branches should be shown
     */
    AllBranchTopComponent(Observable<IRepository> pRepository, ITopComponentDisplayer pTopComponentDisplayer) {
        super(pRepository);
        setLayout(new BorderLayout());
        add(windowProvider.getBranchListWindow(pRepository), BorderLayout.CENTER);

        //Set the displayname in the TopComponent of NetBeans.
        displayNameDisposable = pRepository.subscribe(pRepo -> SwingUtilities.invokeLater(() -> {
            setDisplayName("Git Branches - " + pRepo.getDirectory()); //todo I18N
        }));
    }

    @Override
    protected String getTopComponentName() {
        return ("Branches");
    }
}
