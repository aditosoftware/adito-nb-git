package de.adito.git.gui.icon;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;


/**
 * @author m.kaspera 25.10.2018
 */
public class SwingIconLoaderImpl implements IIconLoader
{

  private Image defaultIcon = icon2Image(MissingIcon.get16x16());

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public ImageIcon getIcon(@NotNull String pIconBase)
  {
    if (!pIconBase.startsWith("/"))
      pIconBase = "/" + pIconBase;
    URL resource = getClass().getResource(pIconBase);
    // return the default icon (signalling a missing icon) instead of null
    if (resource == null)
      return new ImageIcon(defaultIcon);
    else
      return new ImageIcon(resource);
  }

  /**
   * converts an Icon to a image so that it can be used as an ImageIcon
   *
   * @param pIcon Icon to be used as an Image/Imagecon
   * @return Image of the Icon
   */
  private Image icon2Image(Icon pIcon)
  {
    if (pIcon instanceof ImageIcon)
    {
      return ((ImageIcon) pIcon).getImage();
    }
    else
    {
      BufferedImage image = new BufferedImage(pIcon.getIconWidth(), pIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics g = image.getGraphics();
      pIcon.paintIcon(new JLabel(), g, 0, 0);
      g.dispose();
      return image;
    }
  }
}