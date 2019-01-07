package de.adito.git.gui;

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
  EditorKit getEditorKit(String pFileDirectory);

}
