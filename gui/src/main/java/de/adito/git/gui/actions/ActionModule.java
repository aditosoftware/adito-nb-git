package de.adito.git.gui.actions;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ActionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IActionProvider.class).to(ActionProvider.class);
        install(new FactoryModuleBuilder().build(IActionFactory.class));
    }
}
