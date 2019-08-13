package de.adito.git.gui.icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


/**
 * @author m.kaspera 25.10.2018
 */
public class SwingIconLoaderImpl implements IIconLoader
{
  /**
   * {@inheritDoc}
   */
  @Override
  public @Nullable ImageIcon getIcon(@NotNull String pIconBase)
  {
    if (!pIconBase.startsWith("/"))
      pIconBase = "/" + pIconBase;
    return new ImageIcon(getClass().getResource(pIconBase));
  }
}