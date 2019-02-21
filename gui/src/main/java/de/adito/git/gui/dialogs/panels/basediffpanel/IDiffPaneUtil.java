package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;

import javax.swing.*;
import java.util.List;

/**
 * @author m.kaspera, 21.02.2019
 */
public interface IDiffPaneUtil
{

  /**
   * moves the caret to the position of the next changed chunk, as seen from the current position of the caret
   *
   * @param pEditorPane JEditorPane that is currently focused and whose content the IFileChangeChunks of the pModel describe
   * @param pModel      DiffPanelModel that contains the list of IFileChangeChunks describing the contents of pEditorPane and methods to get the
   *                    end/start lines of a specific chunk
   */
  static void moveCaretToNextChunk(JEditorPane pEditorPane, DiffPanelModel pModel)
  {
    int caretLine = pEditorPane.getDocument().getDefaultRootElement().getElementIndex(pEditorPane.getCaret().getDot());
    int moveToElementStartLine = 0;
    List<IFileChangeChunk> changeChunks = pModel.getFileChangesObservable().blockingFirst().getNewValue();
    for (IFileChangeChunk changeChunk : changeChunks)
    {
      if (changeChunk.getChangeType() != EChangeType.SAME && pModel.getGetStartLine().apply(changeChunk) > caretLine)
      {
        moveToElementStartLine = pModel.getGetStartLine().apply(changeChunk);
        break;
      }
    }
    pEditorPane.getCaret().setDot(pEditorPane.getDocument().getDefaultRootElement().getElement(moveToElementStartLine).getStartOffset());
    pEditorPane.requestFocus();
  }

  /**
   * moves the caret to the position of the previous changed chunk, as seen from the current position of the caret
   *
   * @param pEditorPane JEditorPane that is currently focused and whose content the IFileChangeChunks of the pModel describe
   * @param pModel      DiffPanelModel that contains the list of IFileChangeChunks describing the contents of pEditorPane and methods to get the
   *                    end/start lines of a specific chunk
   */
  static void moveCaretToPreviousChunk(JEditorPane pEditorPane, DiffPanelModel pModel)
  {
    int caretLine = pEditorPane.getDocument().getDefaultRootElement().getElementIndex(pEditorPane.getCaret().getDot());
    int moveToElementStartLine = 0;
    List<IFileChangeChunk> changeChunks = pModel.getFileChangesObservable().blockingFirst().getNewValue();
    for (int index = changeChunks.size() - 1; index >= 0; index--)
    {
      if (changeChunks.get(index).getChangeType() != EChangeType.SAME && pModel.getGetEndLine().apply(changeChunks.get(index)) <= caretLine)
      {
        moveToElementStartLine = pModel.getGetStartLine().apply(changeChunks.get(index));
        break;
      }
    }
    pEditorPane.getCaret().setDot(pEditorPane.getDocument().getDefaultRootElement().getElement(moveToElementStartLine).getStartOffset());
    pEditorPane.requestFocus();
  }

}
