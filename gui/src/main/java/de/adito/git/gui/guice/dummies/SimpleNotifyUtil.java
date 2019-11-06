package de.adito.git.gui.guice.dummies;

import de.adito.git.api.INotifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * @author w.glanzer, 07.02.2019
 */
public class SimpleNotifyUtil implements INotifyUtil
{
  @Override
  public void notify(@Nullable String pTitle, @Nullable String pMessage, boolean pAutoDispose)
  {
    notify(pTitle, pMessage, pAutoDispose, null);
  }

  @Override
  public void notify(@Nullable String pTitle, @Nullable String pMessage, boolean pAutoDispose, @Nullable ActionListener pActionListener)
  {
    System.err.println(pTitle + " " + pMessage);
  }

  @Override
  public void notify(@NotNull Exception pEx, @Nullable String pMessage, boolean pAutoDispose)
  {
    notify(pEx, pMessage, pAutoDispose, null);
  }

  @Override
  public void notify(@NotNull Exception pEx, @Nullable String pMessage, boolean pAutoDispose, @Nullable ActionListener pActionListener)
  {
    notify("Encountered Exception: " + pEx.getClass().getSimpleName(), pMessage + "\n\n" + pEx.getMessage() + "\n\n" + Arrays.toString(pEx.getStackTrace()),
           pAutoDispose, pActionListener);
  }
}
