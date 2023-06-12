package de.adito.git.gui.guice.dummies;

import de.adito.git.gui.IEditorKitProvider;
import lombok.NonNull;

import javax.swing.text.EditorKit;
import javax.swing.text.StyledEditorKit;

/**
 * @author w.glanzer, 07.02.2019
 */
public class SimpleEditorKitProvider implements IEditorKitProvider
{
  @NonNull
  @Override
  public EditorKit getEditorKit(@NonNull String pFileDirectory)
  {
    return new StyledEditorKit();
  }

  @NonNull
  @Override
  public EditorKit getEditorKitForContentType(@NonNull String pContentType)
  {
    return new StyledEditorKit();
  }
}
