package de.adito.git.nbm.topComponents;

import de.adito.git.api.IRepository;
import de.adito.git.gui.BranchListWindow;
import de.adito.git.gui.ITopComponentDisplayer;
import io.reactivex.Observable;
import org.openide.windows.TopComponent;

import java.awt.*;

/**
 * A {@link TopComponent} that show all branches of one repository
 *
 * @author a.arnold, 23.10.2018
 */

@TopComponent.Description(preferredID = "AllBranchTopComponent", persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = AllBranchTopComponent.MODE, openAtStartup = false)
class AllBranchTopComponent extends AbstractRepositoryTopComponent {
    static final String MODE = "output";

    /**
     * @param pRepository The repository of which all branches should be shown
     */
    AllBranchTopComponent(Observable<IRepository> pRepository, ITopComponentDisplayer pTopComponentDisplayer) {
        super(pRepository);
        setLayout(new BorderLayout());
        add(new BranchListWindow(pRepository, pTopComponentDisplayer), BorderLayout.CENTER);
    }

    @Override
    protected String getTopComponentName() {
        return ("Branches");
    }
}
