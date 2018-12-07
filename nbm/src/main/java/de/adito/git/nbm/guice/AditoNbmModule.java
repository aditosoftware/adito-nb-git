package de.adito.git.nbm.guice;

import com.google.inject.*;
import com.google.inject.assistedinject.*;
import de.adito.git.api.*;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.dialogs.IDialogDisplayer;
import de.adito.git.gui.guice.*;
import de.adito.git.gui.window.IWindowProvider;
import de.adito.git.impl.IFileSystemObserverProvider;
import de.adito.git.nbm.FileSystemObserverProviderImpl;
import de.adito.git.nbm.*;
import de.adito.git.nbm.dialogs.NBDialogsModule;
import de.adito.git.nbm.window.NBTopComponentsModule;

/**
 * Module for Injector bindings in the nbm module
 *
 * @author m.kaspera 28.09.2018
 */
public class AditoNbmModule extends AbstractModule
{

  @Override
  protected void configure()
  {
    install(GuiceUtil.filterModule(new AditoGitModule(), Key.get(IDialogDisplayer.class), Key.get(IWindowProvider.class),
                                   Key.get(IFileSystemObserverProvider.class), Key.get(IUserPreferences.class)));
    install(new NBTopComponentsModule());
    install(new NBDialogsModule());
    install(new FactoryModuleBuilder().build(IFileSystemObserverImplFactory.class));

    bind(IUserPreferences.class).to(UserPreferencesNBImpl.class);
    bind(IRepositoryProviderFactory.class).to(RepositoryProviderFactory.class);
    bind(IFileSystemObserverProvider.class).to(FileSystemObserverProviderImpl.class);
    bind(IFileSystemObserverImplFactory.class)
        .toProvider(FactoryProvider.newFactory(IFileSystemObserverImplFactory.class, FileSystemObserverImpl.class));
    bind(IEditorKitProvider.class).to(EditorKitProviderImpl.class);
    bind(INotifyUtil.class).to(NotifyUtilImpl.class);
  }
}
