package de.adito.git.gui.swing;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileChangeChunk;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.View;
import java.awt.Rectangle;

/**
 * @author m.kaspera, 04.03.2019
 */
public interface IEditorUtils
{

  /**
   * @param pChangeChunk the chunk for which to retrieve the height of the first line in the editor
   * @param pChangeSide  which side of the chunk should be used
   * @param pEditorPane  the editorPane containing the text for which the chunk describes one part
   * @param pView        the View that displays the editor
   * @return the y value of the first line of the changeChunk in the editor
   * @throws BadLocationException if the editor has less lines than the number of the fist line specified in the chunk
   */
  static int getBoundsForChunk(IFileChangeChunk pChangeChunk, EChangeSide pChangeSide, JEditorPane pEditorPane,
                               View pView) throws BadLocationException
  {
    int lineNumber = pChangeChunk.getStart(pChangeSide);
    int startOffset = pEditorPane.getDocument().getDefaultRootElement().getElement(
        Math.min(lineNumber, pEditorPane.getDocument().getDefaultRootElement().getElementCount() - 1))
        .getStartOffset();
    return (int) pView.modelToView(startOffset, new Rectangle(), Position.Bias.Forward).getBounds().getY();
  }

}
