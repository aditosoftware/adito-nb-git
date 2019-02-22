package de.adito.git.gui.guice.dummies;

import de.adito.git.gui.IEditorKitProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.EditorKit;
import javax.swing.text.StyledEditorKit;

/**
 * @author w.glanzer, 07.02.2019
 */
public class SimpleEditorKitProvider implements IEditorKitProvider
{
  @NotNull
  @Override
  public EditorKit getEditorKit(@NotNull String pFileDirectory)
  {
    return new StyledEditorKit();
  }

  @NotNull
  @Override
  public EditorKit getEditorKitForContentType(@NotNull String pContentType)
  {
    return new StyledEditorKit();
  }
}
