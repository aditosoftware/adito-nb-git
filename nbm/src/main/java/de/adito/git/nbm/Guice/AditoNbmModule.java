package de.adito.git.nbm.Guice;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.assistedinject.FactoryProvider;
import de.adito.git.gui.IDialogDisplayer;
import de.adito.git.impl.IFileSystemObserverProvider;
import de.adito.git.nbm.DialogDisplayerImpl;
import de.adito.git.nbm.FileSystemObserverImpl;
import de.adito.git.nbm.FileSystemObserverProviderImpl;

/**
 * Module for Injector bindings in the nbm module
 *
 * @author m.kaspera 28.09.2018
 */
public class AditoNbmModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(IFileSystemObserverImplFactory.class));

        bind(IRepositoryProviderFactory.class).to(RepositoryProviderFactory.class);
        bind(IFileSystemObserverProvider.class).to(FileSystemObserverProviderImpl.class);
        bind(IFileSystemObserverImplFactory.class).toProvider(FactoryProvider.newFactory(IFileSystemObserverImplFactory.class, FileSystemObserverImpl.class));
        bind(IDialogDisplayer.class).to(DialogDisplayerImpl.class);
    }
}
