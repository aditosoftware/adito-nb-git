package de.adito.git.gui.prefs;

import de.adito.git.api.prefs.IPrefStore;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author w.glanzer, 13.12.2018
 */
public class DummyPrefStore implements IPrefStore
{

  private final Map<String, String> store = new HashMap<>();

  @Nullable
  @Override
  public String get(@NonNull String pKey)
  {
    return store.get(pKey);
  }

  @Override
  public @Nullable String get(@NonNull String pModulePath, @NonNull String pKey)
  {
    return store.get(pKey);
  }

  @Override
  public void put(@NonNull String pKey, @Nullable String pValue)
  {
    if(pValue == null)
      store.remove(pKey);
    else
      store.put(pKey, pValue);
  }

}
