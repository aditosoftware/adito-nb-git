package de.adito.git.nbm.window;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.window.content.IWindowContentProvider;
import io.reactivex.Observable;

import java.awt.BorderLayout;
import java.util.Optional;

/**
 * A {@link AbstractRepositoryTopComponent} that shows all branches of one repository
 *
 * @author a.arnold, 23.10.2018
 */
class AllBranchTopComponent extends AbstractRepositoryTopComponent {

    /**
     * @param pRepository The repository of which all branches should be shown
     */
    @Inject
    AllBranchTopComponent(IWindowContentProvider pWindowContentProvider, @Assisted Observable<Optional<IRepository>> pRepository) {
        super(pRepository);
        setLayout(new BorderLayout());
        add(pWindowContentProvider.createBranchListWindowContent(pRepository), BorderLayout.CENTER);
    }

    @Override
    public String getInitialMode() {
        return "output";
    }

    @Override
    protected String getTopComponentName() {
        return ("Branches");
    }

}
