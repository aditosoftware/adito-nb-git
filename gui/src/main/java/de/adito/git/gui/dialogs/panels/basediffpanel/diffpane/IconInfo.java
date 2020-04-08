package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.data.diff.IChangeDelta;

import javax.swing.*;
import java.awt.Rectangle;

/**
 * Contains information about an icon that lets you discard or accept changes of a text
 * Contains:
 * - The ImageIcon for the icon to be drawn
 * - Rectangle that describes the position of the icon
 * - Consumer that defines what is done when the Icon is clicked
 * - IFileChangeChunk to apply the Consumer to
 *
 * @author m.kaspera, 10.01.2019
 */
class IconInfo
{

  private final ImageIcon imageIcon;
  private final Rectangle iconCoordinates;
  private final IChangeDelta changeDelta;

  IconInfo(ImageIcon pImageIcon, int pYCoordinate, int pXCoordinate, IChangeDelta pChangeDelta)
  {
    imageIcon = pImageIcon;
    iconCoordinates = new Rectangle(pXCoordinate, pYCoordinate, pImageIcon.getIconWidth(), pImageIcon.getIconHeight());
    changeDelta = pChangeDelta;
  }

  /**
   * @return the ImageIcon that is used for displaying this IconInfo
   */
  ImageIcon getImageIcon()
  {
    return imageIcon;
  }

  /**
   * @return the IFileChangeChunk connected to this IconInfo
   */
  IChangeDelta getChangeDelta()
  {
    return changeDelta;
  }

  /**
   * @return Coordinates of this Icon as an Rectangle around the borders of the ImageIcon
   */
  Rectangle getIconCoordinates()
  {
    return iconCoordinates;
  }

}
