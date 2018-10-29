package de.adito.git.gui.dialogs;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author m.kaspera 26.10.2018
 */
public class DialogModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IDialogProvider.class).to(DialogProviderImpl.class);
        install(new FactoryModuleBuilder().build(IDialogFactory.class));
    }

}
