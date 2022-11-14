package de.adito.git.nbm.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import de.adito.git.api.*;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.data.diff.ImportResolveOption;
import de.adito.git.data.diff.ResolveOptionsProviderImpl;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.dialogs.IDialogDisplayer;
import de.adito.git.gui.guice.AditoGitModule;
import de.adito.git.gui.guice.GuiceUtil;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.window.IWindowProvider;
import de.adito.git.impl.IFileSystemObserverProvider;
import de.adito.git.impl.data.diff.*;
import de.adito.git.nbm.*;
import de.adito.git.nbm.dialogs.NBDialogsModule;
import de.adito.git.nbm.icon.NBIconLoader;
import de.adito.git.nbm.prefs.NBPrefStore;
import de.adito.git.nbm.progress.AsyncProgressFacadeImpl;
import de.adito.git.nbm.quicksearch.QuickSearchProviderImpl;
import de.adito.git.nbm.util.SaveUtilImpl;
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
                                   Key.get(IFileSystemObserverProvider.class), Key.get(IUserPreferences.class), Key.get(IAsyncProgressFacade.class),
                                   Key.get(IPrefStore.class), Key.get(IIconLoader.class), Key.get(IFileSystemUtil.class),
                                   Key.get(IEditorKitProvider.class), Key.get(INotifyUtil.class), Key.get(IKeyStore.class),
                                   Key.get(IQuickSearchProvider.class)));
    install(new NBTopComponentsModule());
    install(new NBDialogsModule());
    install(new FactoryModuleBuilder().build(IRepositoryProviderFactory.class));

    bind(IUserPreferences.class).to(UserPreferencesNBImpl.class);
    bind(IFileSystemObserverProvider.class).to(FileSystemObserverProviderImpl.class);
    bind(IEditorKitProvider.class).to(EditorKitProviderImpl.class);
    bind(INotifyUtil.class).to(NotifyUtilImpl.class);
    bind(IAsyncProgressFacade.class).to(AsyncProgressFacadeImpl.class);
    bind(IPrefStore.class).to(NBPrefStore.class);
    bind(IIconLoader.class).to(NBIconLoader.class);
    bind(IFileSystemUtil.class).to(NBFileSystemUtilImpl.class);
    bind(IKeyStore.class).to(KeyStoreImpl.class);
    bind(IQuickSearchProvider.class).to(QuickSearchProviderImpl.class);
    bind(ISaveUtil.class).to(SaveUtilImpl.class);
    Multibinder<ResolveOption> resolveOptionMultibinder = Multibinder.newSetBinder(binder(), ResolveOption.class);
    resolveOptionMultibinder.addBinding().to(WordBasedResolveOption.class);
    resolveOptionMultibinder.addBinding().to(SameResolveOption.class);
    resolveOptionMultibinder.addBinding().to(EnclosedResolveOption.class);
    resolveOptionMultibinder.addBinding().to(ImportResolveOption.class);
    resolveOptionMultibinder.addBinding().to(LiquibaseResolveOption.class);
    resolveOptionMultibinder.addBinding().to(LanguageFileResolveOption.class);
    bind(ResolveOptionsProvider.class).to(ResolveOptionsProviderImpl.class);
  }
}
