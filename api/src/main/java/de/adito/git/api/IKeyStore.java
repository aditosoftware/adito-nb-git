package de.adito.git.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author m.kaspera, 21.12.2018
 */
public interface IKeyStore
{

  /**
   * Saves a key to the store. If it could not be saved, does nothing. If the key already existed, overwrites the password.
   *
   * @param pKey         identifier for the password
   * @param pPassword    sensitive information that should be stored
   * @param pDescription optional description
   */
  void save(@NotNull String pKey, @NotNull char[] pPassword, @Nullable String pDescription);

  /**
   * Deletes a key from the store. If the key was not in the store to begin with, does nothing.
   *
   * @param pKey identifier for the password
   */
  void delete(@NotNull String pKey);

  /**
   * Reads a key from the store.
   *
   * @param pKey identifier for the password
   * @return the password stored under the passed key
   */
  char[] read(@NotNull String pKey);

}
