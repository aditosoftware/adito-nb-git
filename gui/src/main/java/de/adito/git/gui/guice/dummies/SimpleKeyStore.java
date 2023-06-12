package de.adito.git.gui.guice.dummies;

import de.adito.git.api.IKeyStore;
import lombok.NonNull;
import org.jetbrains.annotations.*;

/**
 * @author w.glanzer, 07.02.2019
 */
public class SimpleKeyStore implements IKeyStore
{
  @Override
  public void save(@NonNull String pKey, @NonNull char[] pPassword, @Nullable String pDescription)
  {

  }

  @Override
  public void delete(@NonNull String pKey)
  {

  }

  @Override
  public char[] read(@NonNull String pKey)
  {
    return new char[0];
  }
}
