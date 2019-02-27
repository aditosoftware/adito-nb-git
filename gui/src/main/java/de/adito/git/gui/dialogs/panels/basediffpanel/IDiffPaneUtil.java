package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.JTextComponent;
import java.util.List;

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
   * @param pChangeSide    which side of a IFileChangeChunk should be taken
   */
  static void moveCaretToNextChunk(JTextComponent pTextComponent, List<IFileChangeChunk> pChangeChunks,
                                   EChangeSide pChangeSide)
  {
    int caretLine = pTextComponent.getDocument().getDefaultRootElement().getElementIndex(pTextComponent.getCaret().getDot());
    int moveToElementStartLine = 0;
    for (IFileChangeChunk changeChunk : pChangeChunks)
    {
      if (changeChunk.getChangeType() != EChangeType.SAME && changeChunk.getStart(pChangeSide) > caretLine)
      {
        moveToElementStartLine = changeChunk.getStart(pChangeSide);
        break;
      }
    }
    pTextComponent.getCaret().setDot(pTextComponent.getDocument().getDefaultRootElement().getElement(moveToElementStartLine).getStartOffset());
    pTextComponent.requestFocus();
  }

  /**
   * moves the caret to the position of the previous changed chunk, as seen from the current position of the caret
   *
   * @param pTextComponent JTextComponent that is currently focused and whose content pChangeChunks describes
   * @param pChangeChunks  List of IFileChangeChunks
   * @param pChangeSide    which side of a IFileChangeChunk should be taken
   */
  static void moveCaretToPreviousChunk(JTextComponent pTextComponent, List<IFileChangeChunk> pChangeChunks, EChangeSide pChangeSide)
  {
    int caretLine = pTextComponent.getDocument().getDefaultRootElement().getElementIndex(pTextComponent.getCaret().getDot());
    int moveToElementStartLine = 0;
    for (int index = pChangeChunks.size() - 1; index >= 0; index--)
    {
      if (pChangeChunks.get(index).getChangeType() != EChangeType.SAME && pChangeChunks.get(index).getEnd(pChangeSide) <= caretLine)
      {
        moveToElementStartLine = pChangeChunks.get(index).getStart(pChangeSide);
        break;
      }
    }
    pTextComponent.getCaret().setDot(pTextComponent.getDocument().getDefaultRootElement().getElement(moveToElementStartLine).getStartOffset());
    pTextComponent.requestFocus();
  }

  /**
   * moves the caret to the position of the given changed chunk
   *
   * @param pTextComponent JTextComponent that is currently focused and whose content pChangeChunks describes
   * @param pMoveToChunk   The chunk that the caret should be moved to
   * @param pChangeSide    which side of a IFileChangeChunk should be taken
   */
  static void moveCaretToChunk(@NotNull JTextComponent pTextComponent, @NotNull IFileChangeChunk pMoveToChunk,
                               EChangeSide pChangeSide)
  {
    int moveToElementStartLine = pMoveToChunk.getStart(pChangeSide);
    pTextComponent.getCaret().setDot(pTextComponent.getDocument().getDefaultRootElement().getElement(moveToElementStartLine).getStartOffset());
    pTextComponent.requestFocus();
  }

}
