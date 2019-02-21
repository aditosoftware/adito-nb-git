package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;

import javax.swing.text.JTextComponent;
import java.util.List;
import java.util.function.Function;

/**
 * @author m.kaspera, 21.02.2019
 */
public interface IDiffPaneUtil
{

  /**
   * moves the caret to the position of the next changed chunk, as seen from the current position of the caret
   *
   * @param pTextComponent JTextComponent that is currently focused and whose content the IFileChangeChunks of the pModel describe
   * @param pChangeChunks  List of IFileChangeChunks
   * @param pGetStartLine  Function that return the correct start line of the IFileChangeChunk for the pTextComponent
   */
  static void moveCaretToNextChunk(JTextComponent pTextComponent, List<IFileChangeChunk> pChangeChunks,
                                   Function<IFileChangeChunk, Integer> pGetStartLine)
  {
    int caretLine = pTextComponent.getDocument().getDefaultRootElement().getElementIndex(pTextComponent.getCaret().getDot());
    int moveToElementStartLine = 0;
    for (IFileChangeChunk changeChunk : pChangeChunks)
    {
      if (changeChunk.getChangeType() != EChangeType.SAME && pGetStartLine.apply(changeChunk) > caretLine)
      {
        moveToElementStartLine = pGetStartLine.apply(changeChunk);
        break;
      }
    }
    pTextComponent.getCaret().setDot(pTextComponent.getDocument().getDefaultRootElement().getElement(moveToElementStartLine).getStartOffset());
    pTextComponent.requestFocus();
  }

  /**
   * moves the caret to the position of the previous changed chunk, as seen from the current position of the caret
   *  @param pTextComponent JTextComponent that is currently focused and whose content pChangeChunks describes
   * @param pChangeChunks  List of IFileChangeChunks
   * @param pGetStartLine  Function that return the correct start line of the IFileChangeChunk for the pTextComponent
   * @param pGetEndLine    Function that return the correct ending line of the IFileChangeChunk for the pTextComponent
   */
  static void moveCaretToPreviousChunk(JTextComponent pTextComponent, List<IFileChangeChunk> pChangeChunks,
                                       Function<IFileChangeChunk, Integer> pGetStartLine, Function<IFileChangeChunk, Integer> pGetEndLine)
  {
    int caretLine = pTextComponent.getDocument().getDefaultRootElement().getElementIndex(pTextComponent.getCaret().getDot());
    int moveToElementStartLine = 0;
    for (int index = pChangeChunks.size() - 1; index >= 0; index--)
    {
      if (pChangeChunks.get(index).getChangeType() != EChangeType.SAME && pGetEndLine.apply(pChangeChunks.get(index)) <= caretLine)
      {
        moveToElementStartLine = pGetStartLine.apply(pChangeChunks.get(index));
        break;
      }
    }
    pTextComponent.getCaret().setDot(pTextComponent.getDocument().getDefaultRootElement().getElement(moveToElementStartLine).getStartOffset());
    pTextComponent.requestFocus();
  }

}
