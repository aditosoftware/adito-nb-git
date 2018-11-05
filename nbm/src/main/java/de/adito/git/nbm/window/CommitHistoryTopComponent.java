package de.adito.git.nbm.window;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.window.content.IWindowContentProvider;
import io.reactivex.Observable;

import java.awt.*;
import java.util.List;

/**
 * A {@link AbstractRepositoryTopComponent} that shows the commit history window
 *
 * @author a.arnold, 24.10.2018
 */
class CommitHistoryTopComponent extends AbstractRepositoryTopComponent {

    private final String displayableContext;

    @Inject
    CommitHistoryTopComponent(IWindowContentProvider pWindowContentProvider, @Assisted Observable<IRepository> pRepository,
                              @Assisted List<ICommit> pCommits, @Assisted String pDisplayableContext) {
        super(pRepository);
        displayableContext = pDisplayableContext;
        setLayout(new BorderLayout());
        add(pWindowContentProvider.createCommitHistoryWindowContent(pCommits), BorderLayout.CENTER);
    }

    @Override
    public String getInitialMode() {
        return "output";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTopComponentName() {
        return ("Commits - " + displayableContext);
    }
}
