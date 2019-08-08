package de.adito.git.api.icon;

import org.jetbrains.annotations.*;

import javax.swing.*;

/**
 * Interface to load Icons depending on the Theme.
 *
 * @author m.haertel, 06.08.2019
 */
public interface IIconLoader
{
  /**
   * Returns an Icon for a given IconBase.
   * Depending on the current Theme a dark or bright Icon is provided.
   *
   * @param pIconBase the IconBase
   * @return the Icon
   */
  @Nullable
  ImageIcon getIcon(@NotNull String pIconBase);
}
