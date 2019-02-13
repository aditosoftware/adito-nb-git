package de.adito.git.gui;

import org.jetbrains.annotations.*;

import javax.swing.text.EditorKit;

/**
 * @author m.kaspera 13.11.2018
 */
public interface IEditorKitProvider
{
  /**
   * @param pFileDirectory The file directory of the file to check the mime type
   * @return EditorKit that deals with the provided mime type
   */
  @Nullable
  EditorKit getEditorKit(@NotNull String pFileDirectory);

  /**
   * @param pContentType The specific content type / mime type
   * @return EditorKit that deals with the provided mime type
   */
  @Nullable
  EditorKit getEditorKitForContentType(@NotNull String pContentType);

}
