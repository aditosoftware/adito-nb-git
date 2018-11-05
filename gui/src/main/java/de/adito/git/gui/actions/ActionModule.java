package de.adito.git.gui.actions;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * A Guice module to define bindings for the action package
 *
 * @author a.arnold 05.11.2018
 */
public class ActionModule extends AbstractModule {


    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(IActionFactory.class));
        bind(IActionProvider.class).to(ActionProvider.class);
    }
}
