package de.adito.git.gui.dialogs.panels.TextPanes;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.TextHighlightUtil;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.util.List;

/**
 * @author m.kaspera, 13.12.2018
 */
public class ForkPointPaneWrapper implements IDiscardable
{

  private final JScrollPane textScrollPane;
  private final JTextPane textPane;
  private final IMergeDiff mergeDiff;
  private final Disposable disposable;
  private final _PaneDocumentListener paneDocumentListener = new _PaneDocumentListener();
  private int caretPosition;

  public ForkPointPaneWrapper(IMergeDiff pMergeDiff)
  {
    mergeDiff = pMergeDiff;
    textPane = new NonWrappingTextPane();
    textScrollPane = new JScrollPane(textPane);
    textPane.getDocument().addDocumentListener(paneDocumentListener);
    disposable = Observable.zip(
        mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges().getChangeChunks(),
        mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS).getFileChanges().getChangeChunks(), _ListPair::new)
        .subscribe(this::_refreshContent);
  }

  public JScrollPane getPane()
  {
    return textScrollPane;
  }

  public JTextPane getTextPane()
  {
    return textPane;
  }

  private void _refreshContent(_ListPair pChangeChunkLists)
  {
    paneDocumentListener.disable();
    caretPosition = textPane.getCaretPosition();
    // EChangeSide.OLD because the content of the ForkPointTextPane is the version of the forkPoint (i.e. the old version in all cases since forkPoint
    // predates both commits)
    TextHighlightUtil.insertColoredText(textPane, pChangeChunkLists.yourVersion, pChangeChunkLists.theirVersion,
                                        IFileChangeChunk::getALines, pFileChangeChunk -> "");
    SwingUtilities.invokeLater(() -> textPane.setCaretPosition(caretPosition));
    paneDocumentListener.enable();
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
          SwingUtilities.invokeLater(() -> {
            caretPosition = textPane.getCaretPosition() + insertedText.length();
            mergeDiff.insertText(insertedText, insertedText.length(), insertOffset, true);
          });
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
        caretPosition = textPane.getCaretPosition();
        SwingUtilities.invokeLater(() -> mergeDiff.insertText("", pEvent.getLength(), pEvent.getOffset(), false));
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

    _ListPair(List<IFileChangeChunk> yourVersion, List<IFileChangeChunk> theirVersion)
    {
      this.yourVersion = yourVersion;
      this.theirVersion = theirVersion;
    }
  }
}
