package de.adito.git.api;

import lombok.NonNull;
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
  void save(@NonNull String pKey, @NonNull char[] pPassword, @Nullable String pDescription);

  /**
   * Deletes a key from the store. If the key was not in the store to begin with, does nothing.
   *
   * @param pKey identifier for the password
   */
  void delete(@NonNull String pKey);

  /**
   * Reads a key from the store.
   *
   * @param pKey identifier for the password
   * @return the password stored under the passed key
   */
  char[] read(@NonNull String pKey);

}
