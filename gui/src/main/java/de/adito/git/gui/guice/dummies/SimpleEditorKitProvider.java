package de.adito.git.gui.guice.dummies;

import de.adito.git.gui.IEditorKitProvider;

import javax.swing.text.*;

/**
 * @author w.glanzer, 07.02.2019
 */
public class SimpleEditorKitProvider implements IEditorKitProvider
{
  @Override
  public EditorKit getEditorKit(String pFileDirectory)
  {
    return new StyledEditorKit();
  }

  @Override
  public EditorKit getEditorKitForContentType(String pContentType)
  {
    return new StyledEditorKit();
  }
}
