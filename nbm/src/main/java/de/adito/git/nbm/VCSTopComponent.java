package de.adito.git.nbm;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.adito.git.nbm.Guice.AditoNbmModule;
import de.adito.git.api.IRepository;
import de.adito.git.gui.IDialogDisplayer;
import de.adito.git.gui.RepositoryProvider;
import de.adito.git.gui.StatusWindow;
import de.adito.git.gui.guice.AditoGitModule;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;

/**
 * @author m.kaspera 28.09.2018
 */
@TopComponent.Description(preferredID = "VCSTopComponent",
    iconBase = "",
    persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "output", openAtStartup = true, position = 1000)
public class VCSTopComponent extends TopComponent {

    public VCSTopComponent() {
        setLayout(new BorderLayout());
        Injector injector;
        injector = Guice.createInjector(new AditoGitModule(), new AditoNbmModule());
        IRepository git = injector.getInstance(RepositoryProvider.class).getRepositoryImpl();

        StatusWindow statusWindow = new StatusWindow(git.status(), injector.getInstance(IDialogDisplayer.class), git);
        add(new JLabel("test success"), BorderLayout.NORTH);
        add(statusWindow, BorderLayout.CENTER);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
    }
}
