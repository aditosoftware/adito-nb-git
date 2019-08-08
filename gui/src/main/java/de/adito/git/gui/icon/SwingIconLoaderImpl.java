package de.adito.git.gui.icon;

import de.adito.git.api.icon.IIconLoader;
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
  public ImageIcon getIcon(@NotNull String pIconBase)
  {
    return new ImageIcon(getClass().getResource(pIconBase));
  }
}