package de.adito.git.nbm.prefs;

import com.google.inject.Singleton;
import de.adito.git.api.prefs.IPrefStore;
import org.jetbrains.annotations.*;
import org.openide.util.NbPreferences;

import java.util.prefs.Preferences;

/**
 * PrefStore-Impl backed up by NetBeans Preferences
 *
 * @author w.glanzer, 13.12.2018
 */
@Singleton
public class NBPrefStore implements IPrefStore
{

  private final Preferences preferences = NbPreferences.forModule(IPrefStore.class);

  @Nullable
  @Override
  public String get(@NotNull String pKey)
  {
    return preferences.get(pKey, null);
  }

  @Override
  public void put(@NotNull String pKey, @Nullable String pValue)
  {
    if(pValue != null)
      preferences.put(pKey, pValue);
    else
      preferences.remove(pKey);
  }

}
