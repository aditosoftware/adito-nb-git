package de.adito.git.gui.guice;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.*;
import de.adito.git.api.*;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.*;
import de.adito.git.gui.actions.ActionModule;
import de.adito.git.gui.dialogs.*;
import de.adito.git.gui.guice.dummies.*;
import de.adito.git.gui.icon.*;
import de.adito.git.gui.prefs.DummyPrefStore;
import de.adito.git.gui.progress.SimpleAsyncProgressFacade;
import de.adito.git.gui.window.WindowModule;
import de.adito.git.impl.*;
import de.adito.git.impl.data.DataModule;
import de.adito.git.impl.ssh.AditoSshModule;

/**
 * Handles all the bindings for the injections in the GUI module
 *
 * @author m.kaspera 21.09.2018
 */
@SuppressWarnings("deprecation")
public class AditoGitModule extends AbstractModule
{

  @Override
  protected void configure()
  {
    // Dialog-Modul
    install(new DialogModule());
    install(new ActionModule());
    install(new WindowModule());
    install(new AditoSshModule());
    install(new DataModule());

    // bind IRepository to RepositoryImpl and construct the necessary factory
    install(new FactoryModuleBuilder().build(IRepositoryFactory.class));
    bind(IUserPreferences.class).to(UserPreferencesImpl.class);
    bind(IRepositoryFactory.class).toProvider(FactoryProvider.newFactory(IRepositoryFactory.class, RepositoryImpl.class));
    bind(IIconLoader.class).to(SwingIconLoaderImpl.class);
    bind(IFileSystemObserverProvider.class).to(FileSystemObserverProviderImpl.class);
    bind(IAsyncProgressFacade.class).to(SimpleAsyncProgressFacade.class);
    bind(IPrefStore.class).to(DummyPrefStore.class);
    bind(IUserInputPrompt.class).to(UserInputPromptImpl.class);
    bind(ICloneRepo.class).to(CloneRepoImpl.class);
    bind(IFileSystemUtil.class).to(SimpleFileSystemUtil.class);
    bind(IKeyStore.class).to(SimpleKeyStore.class);
    bind(INotifyUtil.class).to(SimpleNotifyUtil.class);
    bind(IEditorKitProvider.class).to(SimpleEditorKitProvider.class);
    bind(IQuickSearchProvider.class).to(SimpleQuickSearchProvider.class);
  }
}