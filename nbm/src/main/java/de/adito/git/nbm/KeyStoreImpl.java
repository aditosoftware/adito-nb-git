package de.adito.git.nbm;

import de.adito.git.api.IKeyStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.keyring.Keyring;

/**
 * @author m.kaspera, 21.12.2018
 */
public class KeyStoreImpl implements IKeyStore
{
  @Override
  public void save(@NotNull String pKey, @NotNull char[] pPassword, @Nullable String pDescription)
  {
    Keyring.save(pKey, pPassword, pDescription);
  }

  @Override
  public void delete(@NotNull String pKey)
  {
    Keyring.delete(pKey);
  }

  @Override
  public char[] read(@NotNull String pKey)
  {
    return Keyring.read(pKey);
  }
}
