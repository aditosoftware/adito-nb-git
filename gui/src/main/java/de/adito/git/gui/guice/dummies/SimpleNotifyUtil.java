package de.adito.git.gui.guice.dummies;

import de.adito.git.api.INotifyUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author w.glanzer, 07.02.2019
 */
public class SimpleNotifyUtil implements INotifyUtil
{
  @Override
  public void notify(@Nullable String pTitle, @Nullable String pMessage, boolean pAutoDispose)
  {
    System.err.println(pTitle + " " + pMessage);
  }
}
