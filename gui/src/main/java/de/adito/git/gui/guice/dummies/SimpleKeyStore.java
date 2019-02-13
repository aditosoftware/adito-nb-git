package de.adito.git.gui.guice.dummies;

import de.adito.git.api.IKeyStore;
import org.jetbrains.annotations.*;

/**
 * @author w.glanzer, 07.02.2019
 */
public class SimpleKeyStore implements IKeyStore
{
  @Override
  public void save(@NotNull String pKey, @NotNull char[] pPassword, @Nullable String pDescription)
  {

  }

  @Override
  public void delete(@NotNull String pKey)
  {

  }

  @Override
  public char[] read(@NotNull String pKey)
  {
    return new char[0];
  }
}
