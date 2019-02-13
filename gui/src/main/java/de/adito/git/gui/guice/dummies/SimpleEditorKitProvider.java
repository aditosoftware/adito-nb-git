package de.adito.git.gui.guice.dummies;

import de.adito.git.gui.IEditorKitProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.*;

/**
 * @author w.glanzer, 07.02.2019
 */
public class SimpleEditorKitProvider implements IEditorKitProvider
{
  @Override
  public EditorKit getEditorKit(@NotNull String pFileDirectory)
  {
    return new StyledEditorKit();
  }

  @Override
  public EditorKit getEditorKitForContentType(@NotNull String pContentType)
  {
    return new StyledEditorKit();
  }
}
