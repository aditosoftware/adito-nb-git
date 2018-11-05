package de.adito.git.nbm.window;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.adito.git.gui.window.IWindowProvider;

/**
 * A Guice module to define bindings for the window NetBeans package
 *
 * @author a.arnold, 31.10.2018
 */
public class NBTopComponentsModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(ITopComponentFactory.class));
        bind(IWindowProvider.class).to(WindowProviderNBImpl.class);
    }

}
