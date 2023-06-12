package de.adito.git.nbm;

import de.adito.git.api.IKeyStore;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.keyring.Keyring;

/**
 * @author m.kaspera, 21.12.2018
 */
public class KeyStoreImpl implements IKeyStore
{
  @Override
  public void save(@NonNull String pKey, @NonNull char[] pPassword, @Nullable String pDescription)
  {
    Keyring.save(pKey, pPassword, pDescription);
  }

  @Override
  public void delete(@NonNull String pKey)
  {
    Keyring.delete(pKey);
  }

  @Override
  public char[] read(@NonNull String pKey)
  {
    return Keyring.read(pKey);
  }
}
