package de.adito.git.gui;

import org.jetbrains.annotations.NotNull;

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
  @NotNull
  EditorKit getEditorKit(@NotNull String pFileDirectory);

  /**
   * @param pContentType The specific content type / mime type
   * @return EditorKit that deals with the provided mime type
   */
  @NotNull
  EditorKit getEditorKitForContentType(@NotNull String pContentType);

}
