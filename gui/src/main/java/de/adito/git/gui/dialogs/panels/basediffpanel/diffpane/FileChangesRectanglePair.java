package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.data.IFileChangesEvent;

import java.awt.Rectangle;

/**
 * Simple class for a pair of IFileChangeEvent and a Rectangle (such as in a viewport)
 *
 * @author m.kaspera, 15.01.2019
 */
class FileChangesRectanglePair
{

  private final IFileChangesEvent fileChangesEvent;
  private final Rectangle rectangle;

  FileChangesRectanglePair(IFileChangesEvent pFileChangesEvent, Rectangle pRectangle)
  {
    fileChangesEvent = pFileChangesEvent;
    rectangle = pRectangle;
  }

  IFileChangesEvent getFileChangesEvent()
  {
    return fileChangesEvent;
  }

  Rectangle getRectangle()
  {
    return rectangle;
  }
}
