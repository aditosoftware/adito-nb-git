package de.adito.git.nbm.icon;

import de.adito.git.gui.icon.AbstractIconLoader;
import org.jetbrains.annotations.*;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.util.HashMap;

/**
 * IconLoader Implementation backed up by NetBeans ImageUtilities.
 *
 * @author m.haertel, 06.08.2019
 */
public class NBIconLoader extends AbstractIconLoader
{
  private static HashMap<String, ImageIcon> iconCache = new HashMap<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nullable ImageIcon getIcon(@NotNull String pIconBase)
  {
    if (iconCache.containsKey(pIconBase)) {
      return iconCache.get(pIconBase);
    } else {
      ImageIcon icon = ImageUtilities.loadImageIcon(pIconBase, true);
      iconCache.put(pIconBase, icon);
      return icon;
    }
  }
}
