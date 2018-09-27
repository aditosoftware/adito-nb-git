package de.adito.git.Guice;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.assistedinject.FactoryProvider;
import de.adito.git.MockRepositoryDescriptionImpl;
import de.adito.git.RepositoryImpl;
import de.adito.git.api.data.IRepositoryDescription;

/**
 * @author m.kaspera 21.09.2018
 */
@SuppressWarnings( "deprecation" )
public class AditoGitModule extends AbstractModule {

    @Override
    protected void configure (){
        install(new FactoryModuleBuilder().build(IRepositoryFactory.class));
        bind(IRepositoryFactory.class).toProvider(FactoryProvider.newFactory(IRepositoryFactory.class, RepositoryImpl.class));
        bind(IRepositoryDescription.class).to(MockRepositoryDescriptionImpl.class);
    }
}
