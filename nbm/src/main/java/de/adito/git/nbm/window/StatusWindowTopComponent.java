package de.adito.git.nbm.window;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.window.content.IWindowContentProvider;
import io.reactivex.Observable;

import java.awt.*;

/**
 * A {@link AbstractRepositoryTopComponent} that shows the status of changed files in the project
 *
 * @author m.kaspera 06.11.2018
 */
public class StatusWindowTopComponent extends AbstractRepositoryTopComponent {

    @Inject
    StatusWindowTopComponent(IWindowContentProvider pWindowContentProvider, @Assisted Observable<IRepository> pRepository) {
        super(pRepository);
        setLayout(new BorderLayout());
        add(pWindowContentProvider.createStatusWindowContent(pRepository));
    }

    @Override
    protected String getInitialMode() {
        return "output";
    }

    @Override
    protected String getTopComponentName() {
        return "StatusWindow";
    }
}
