package de.adito.git.gui;

import javax.swing.text.EditorKit;

/**
 * @author m.kaspera 13.11.2018
 */
public interface IEditorKitProvider {

    /**
     * @param mimeType mime type for which an editorKit should be retrieved
     * @return EditorKit that deals with the provided mime type
     */
    EditorKit getEditorKit(String mimeType);

}
