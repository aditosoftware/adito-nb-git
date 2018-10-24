package de.adito.git.nbm.topComponents;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.CommitHistoryWindow;
import io.reactivex.Observable;
import org.openide.windows.TopComponent;

import java.awt.*;

/**
 * @author a.arnold, 24.10.2018
 */

@TopComponent.Description(preferredID = "AllCommitsTopComponent", persistenceType = TopComponent.PERSISTENCE_NEVER)
// TODO: 24.10.2018 mode checking. can't find the mode for "output"?
@TopComponent.Registration(mode = AllCommitsTopComponent.MODE, openAtStartup = false)

class AllCommitsTopComponent extends AbstractRepositoryTopComponent {
    static final String MODE = "output";
    private IBranch branch;

    /**
     * @param pBranch
     * @throws Exception
     */
    AllCommitsTopComponent(Observable<IRepository> pRepository, IBranch pBranch) throws Exception {
        super(pRepository);
        branch = pBranch;
        setLayout(new BorderLayout());
        add(new CommitHistoryWindow(pRepository, pBranch), BorderLayout.CENTER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTopComponentName() {
        return ("Commits - " + branch.getSimpleName());
    }
}
