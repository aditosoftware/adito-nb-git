package de.adito.git.gui.icon;

import org.jetbrains.annotations.*;

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
    return new ImageIcon(getClass().getResource(pIconBase));
  }
}