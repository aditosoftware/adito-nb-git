package de.adito.git.nbm.Guice;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryProvider;
import de.adito.git.gui.*;
import de.adito.git.nbm.DialogDisplayerImpl;

/**
 * Module for Injector bindings in the nbm module
 *
 * @author m.kaspera 28.09.2018
 */
public class AditoNbmModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IRepositoryProviderFactory.class).toProvider(FactoryProvider.newFactory(IRepositoryProviderFactory.class, RepositoryProvider.class));
        bind(IDialogDisplayer.class).to(DialogDisplayerImpl.class);
    }
}
