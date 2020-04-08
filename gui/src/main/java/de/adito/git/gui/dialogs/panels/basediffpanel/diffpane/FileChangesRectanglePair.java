package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.data.diff.IDeltaTextChangeEvent;

import java.awt.Rectangle;

/**
 * Simple class for a pair of IFileChangeEvent and a Rectangle (such as in a viewport)
 *
 * @author m.kaspera, 15.01.2019
 */
class FileChangesRectanglePair
{

  private final IDeltaTextChangeEvent changeEvent;
  private final Rectangle rectangle;

  FileChangesRectanglePair(IDeltaTextChangeEvent pChangeEvent, Rectangle pRectangle)
  {
    changeEvent = pChangeEvent;
    rectangle = pRectangle;
  }

  IDeltaTextChangeEvent getChangeEvent()
  {
    return changeEvent;
  }

  Rectangle getRectangle()
  {
    return rectangle;
  }
}
