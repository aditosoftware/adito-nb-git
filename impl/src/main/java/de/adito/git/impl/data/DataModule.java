package de.adito.git.impl.data;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.data.diff.ResolveOptionsProviderImpl;
import de.adito.git.impl.data.diff.*;
import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionListener;

/**
 * Guice Module for the data package
 *
 * @author m.kaspera, 24.12.2018
 */
public class DataModule extends AbstractModule
{

  @Override
  protected void configure()
  {
    install(new FactoryModuleBuilder().build(IDataFactory.class));
    Multibinder<ResolveOption> resolveOptionMultibinder = Multibinder.newSetBinder(binder(), ResolveOption.class);
    resolveOptionMultibinder.addBinding().to(WordBasedResolveOption.class);
    resolveOptionMultibinder.addBinding().to(SameResolveOption.class);
    resolveOptionMultibinder.addBinding().to(EnclosedResolveOption.class);
    bind(ResolveOptionsProvider.class).to(ResolveOptionsProviderImpl.class);

    // Dummy bindings
    bind(IKeyStore.class).to(KeyStoreDummy.class);
    bind(INotifyUtil.class).to(NotifyUtilDummy.class);
    bind(IPrefStore.class).to(PrefStoreDummy.class);
  }

  static class KeyStoreDummy implements IKeyStore
  {

    @Override
    public void save(@NonNull String pKey, @NonNull char[] pPassword, @Nullable String pDescription)
    {
      throw new NotImplementedException();
    }

    @Override
    public void delete(@NonNull String pKey)
    {
      throw new NotImplementedException();
    }

    @Override
    public char[] read(@NonNull String pKey)
    {
      throw new NotImplementedException();
    }
  }

  static class NotifyUtilDummy implements INotifyUtil
  {

    @Override
    public void notify(@Nullable String pTitle, @Nullable String pMessage, boolean pAutoDispose)
    {
      throw new NotImplementedException();
    }

    @Override
    public void notify(@Nullable String pTitle, @Nullable String pMessage, boolean pAutoDispose, @Nullable ActionListener pActionListener)
    {
      throw new NotImplementedException();
    }

    @Override
    public void notify(@NonNull Exception pEx, @Nullable String pMessage, boolean pAutoDispose)
    {
      throw new NotImplementedException();
    }

    @Override
    public void notify(@NonNull Exception pEx, @Nullable String pMessage, boolean pAutoDispose, @Nullable ActionListener pActionListener)
    {
      throw new NotImplementedException();
    }
  }

  static class PrefStoreDummy implements IPrefStore
  {

    @Override
    public @Nullable String get(@NonNull String pKey)
    {
      throw new NotImplementedException();
    }

    @Override
    public @Nullable String get(@NonNull String pModulePath, @NonNull String pKey)
    {
      throw new NotImplementedException();
    }

    @Override
    public void put(@NonNull String pKey, @Nullable String pValue)
    {
      throw new NotImplementedException();
    }
  }

}
