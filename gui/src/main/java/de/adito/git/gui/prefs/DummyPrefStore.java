package de.adito.git.gui.prefs;

import de.adito.git.api.prefs.IPrefStore;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * @author w.glanzer, 13.12.2018
 */
public class DummyPrefStore implements IPrefStore
{

  private final Map<String, String> store = new HashMap<>();

  @Nullable
  @Override
  public String get(@NotNull String pKey)
  {
    return store.get(pKey);
  }

  @Override
  public void put(@NotNull String pKey, @Nullable String pValue)
  {
    if(pValue == null)
      store.remove(pKey);
    else
      store.put(pKey, pValue);
  }

}
