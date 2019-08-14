package de.adito.git.nbm.icon;

import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.icon.MissingIcon;
import org.jetbrains.annotations.NotNull;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.util.HashMap;

/**
 * IconLoader Implementation backed up by NetBeans ImageUtilities.
 *
 * @author m.haertel, 06.08.2019
 */
public class NBIconLoader implements IIconLoader
{
  private final static HashMap<String, ImageIcon> iconCache = new HashMap<>();
  private final ImageIcon defaultIcon = new ImageIcon(ImageUtilities.icon2Image(MissingIcon.get16x16()));

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public ImageIcon getIcon(@NotNull String pIconBase)
  {
    if (iconCache.containsKey(pIconBase))
    {
      return iconCache.get(pIconBase);
    }
    else
    {
      ImageIcon icon = ImageUtilities.loadImageIcon(pIconBase, true);
      // return the default icon (signalling a missing icon) instead of null
      if (icon == null)
        return defaultIcon;
      iconCache.put(pIconBase, icon);
      return icon;
    }
  }
}
