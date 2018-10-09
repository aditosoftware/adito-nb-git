package de.adito.git.nbm.Guice;

import com.google.inject.AbstractModule;
import de.adito.git.gui.ITopComponent;
import de.adito.git.gui.SwingTopComponent;
import de.adito.git.nbm.DialogDisplayerImpl;
import de.adito.git.gui.IDialogDisplayer;

/**
 * Module for Injector bindings in the nbm module
 *
 * @author m.kaspera 28.09.2018
 */
public class AditoNbmModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IDialogDisplayer.class).to(DialogDisplayerImpl.class);
        bind(ITopComponent.class).to(SwingTopComponent.class);
    }
}
