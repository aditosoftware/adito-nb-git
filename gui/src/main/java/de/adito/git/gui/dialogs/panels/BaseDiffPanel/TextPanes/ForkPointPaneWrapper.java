package de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.TextHighlightUtil;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.DiffPane.DiffPane;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import java.util.List;

/**
 *  Wrapper around a DiffPane, similar to DiffPaneWrapper. Made so that both use a DiffPane for displaying the LineNumPanels/ChoiceButtonPanels
 *  while having different functionality in the central editorPane
 *
 * @author m.kaspera, 13.12.2018
 */
public class ForkPointPaneWrapper implements IDiscardable
{

  private final JEditorPane editorPane;
  private final DiffPane diffPane;
  private final IMergeDiff mergeDiff;
  private final Disposable disposable;
  private final _PaneDocumentListener paneDocumentListener = new _PaneDocumentListener();

  /**
   * @param pMergeDiff MergeDiff that has all the information about the conflict that should be displayed/resolvable
   */
  public ForkPointPaneWrapper(IMergeDiff pMergeDiff)
  {
    mergeDiff = pMergeDiff;
    editorPane = new JEditorPane();
    // disable manual text input for now, also no need for document listener as long as jEditorPane not editable
    diffPane = new DiffPane(editorPane);
    editorPane.getDocument().addDocumentListener(paneDocumentListener);
    disposable = Observable.zip(
        mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges().getChangeChunks(),
        mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS).getFileChanges().getChangeChunks(), _ListPair::new)
        .subscribe(this::_refreshContent);
  }

  public DiffPane getPane()
  {
    return diffPane;
  }

  public JScrollPane getScrollPane()
  {
    return diffPane.getScrollPane();
  }

  private void _refreshContent(_ListPair pChangeChunkLists)
  {
    if (pChangeChunkLists.doUpdate)
    {
      paneDocumentListener.disable();
      final int scrollBarPos = getScrollPane() != null ? getScrollPane().getVerticalScrollBar().getValue() : 0;
      // OLD because the content of the ForkPointTextPane is the version of the forkPoint (i.e. the old version in all cases since forkPoint
      // predates both commits)
      TextHighlightUtil.insertColoredText(editorPane,
                                          pChangeChunkLists.yourVersion,
                                          pChangeChunkLists.theirVersion,
                                          IFileChangeChunk::getALines,
                                          pFileChangeChunk -> "",
                                          IFileChangeChunk::getAStart,
                                          IFileChangeChunk::getAEnd);
      editorPane.revalidate();
      SwingUtilities.invokeLater(() -> getScrollPane().getVerticalScrollBar().setValue(scrollBarPos));
      paneDocumentListener.enable();
    }
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }

  /**
   * DocumentListener to check for user-input in the fork-point version of the merge conflict
   * Can be manually dis-/enabled if text is input by the code, this is done by calling the
   * methods named disable/enable
   */
  private class _PaneDocumentListener implements DocumentListener
  {

    private boolean isActive = true;

    @Override
    public void insertUpdate(DocumentEvent pEvent)
    {
      if (isActive)
      {
        try
        {
          // get the information about what text and where before the invokeLater(), else the information can be outdated
          final String insertedText = pEvent.getDocument().getText(pEvent.getOffset(), pEvent.getLength());
          final int insertOffset = pEvent.getOffset();
          SwingUtilities.invokeLater(() -> mergeDiff.insertText(insertedText, insertedText.length(), insertOffset, true));
        }
        catch (BadLocationException e1)
        {
          throw new RuntimeException(e1);
        }
      }
    }

    @Override
    public void removeUpdate(DocumentEvent pEvent)
    {
      if (isActive)
      {
        final int removeOffset = pEvent.getOffset();
        SwingUtilities.invokeLater(() -> mergeDiff.insertText("", pEvent.getLength(), removeOffset, false));
      }
    }

    @Override
    public void changedUpdate(DocumentEvent pEvent)
    {
      // changed only triggers on metadata change which is not interesting for this use-case
    }

    /**
     * Enables the processing of events for this listener
     */
    void enable()
    {
      isActive = true;
    }

    /**
     * Disables the processing of events for this listener. Acts as if the listener wouldn't be here if disabled
     */
    void disable()
    {
      isActive = false;
    }
  }

  private static class _ListPair
  {
    List<IFileChangeChunk> yourVersion;
    List<IFileChangeChunk> theirVersion;
    boolean doUpdate;

    _ListPair(IFileChangesEvent yourVersion, IFileChangesEvent theirVersion)
    {
      this.yourVersion = yourVersion.getNewValue();
      this.theirVersion = theirVersion.getNewValue();
      doUpdate = yourVersion.isUpdateUI() && theirVersion.isUpdateUI();
    }
  }
}
