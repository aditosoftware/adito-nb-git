package de.adito.git.nbm.Guice;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.assistedinject.FactoryProvider;
import de.adito.git.gui.IDialogDisplayer;
import de.adito.git.gui.ITopComponent;
import de.adito.git.gui.RepositoryProvider;
import de.adito.git.gui.SwingTopComponent;
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
        bind(IRepositoryProviderFactory.class).toProvider(FactoryProvider.newFactory(IRepositoryProviderFactory.class, RepositoryProvider.class));
        bind(IDialogDisplayer.class).to(DialogDisplayerImpl.class);
        bind(IFileSystemObserverProvider.class).to(FileSystemObserverProviderImpl.class);
        install(new FactoryModuleBuilder().build(IFileSystemObserverImplFactory.class));
        bind(IFileSystemObserverImplFactory.class).toProvider(FactoryProvider.newFactory(IFileSystemObserverImplFactory.class, FileSystemObserverImpl.class));
        bind(ITopComponent.class).to(SwingTopComponent.class);
    }
}
