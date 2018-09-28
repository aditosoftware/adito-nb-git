package de.adito.git.Guice;

import com.google.inject.AbstractModule;
import de.adito.git.DialogDisplayerImpl;
import de.adito.git.IDialogDisplayer;

/**
 * Module for Injector bindings in the nbm module
 *
 * @author m.kaspera 28.09.2018
 */
public class AditoNbmModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IDialogDisplayer.class).to(DialogDisplayerImpl.class);
    }
}
