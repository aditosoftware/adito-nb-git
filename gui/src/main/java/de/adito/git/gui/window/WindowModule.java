package de.adito.git.gui.window;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author m.kaspera 29.10.2018
 */
public class WindowModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IWindowProvider.class).to(WindowProviderImpl.class);
        install(new FactoryModuleBuilder().build(IWindowFactory.class));
    }
}
