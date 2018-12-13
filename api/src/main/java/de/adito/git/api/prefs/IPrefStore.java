package de.adito.git.api.prefs;

import org.jetbrains.annotations.*;

/**
 * Facade to provide preferences and store it on disk
 *
 * @author w.glanzer, 13.12.2018
 */
public interface IPrefStore
{

  /**
   * Returns the value associated with the given key
   *
   * @param pKey Key
   * @return Value or <tt>null</tt>
   */
  @Nullable
  String get(@NotNull String pKey);

  /**
   * Associate a key with a value
   *
   * @param pKey   Key
   * @param pValue Value, <tt>null</tt> if the given key should be removed
   */
  void put(@NotNull String pKey, @Nullable String pValue);

}
