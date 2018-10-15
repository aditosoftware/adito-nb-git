package de.adito.git.gui.guice;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.assistedinject.FactoryProvider;
import de.adito.git.impl.RepositoryImpl;

/**
 * Handles all the bindings for the injections in the GUI module
 *
 * @author m.kaspera 21.09.2018
 */
@SuppressWarnings( "deprecation" )
public class AditoGitModule extends AbstractModule {

    @Override
    protected void configure (){
        // bind IRepository to RepositoryImpl and construct the necessary factory
        install(new FactoryModuleBuilder().build(IRepositoryFactory.class));
        bind(IRepositoryFactory.class).toProvider(FactoryProvider.newFactory(IRepositoryFactory.class, RepositoryImpl.class));
    }
}
