package de.adito.git.gui.dialogs.panels.basediffpanel.textpanes;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.TextHighlightUtil;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.DiffPane;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import java.util.Optional;

/**
 * Wrapper around a DiffPane, similar to DiffPaneWrapper. Made so that both use a DiffPane for displaying the LineNumPanels/ChoiceButtonPanels
 * while having different functionality in the central editorPane
 *
 * @author m.kaspera, 13.12.2018
 */
public class ForkPointPaneWrapper implements IDiscardable, IPaneWrapper
{

  private final JEditorPane editorPane;
  private final DiffPane diffPane;
  private final IMergeDiff mergeDiff;
  private final Disposable mergeDiffDisposable;
  private final Disposable editorKitDisposable;
  private final _PaneDocumentListener paneDocumentListener = new _PaneDocumentListener();

  /**
   * @param pMergeDiff           MergeDiff that has all the information about the conflict that should be displayed/resolvable
   * @param pEditorKitObservable Observable of the editorKit that should be used in the editorPane
   */
  public ForkPointPaneWrapper(IMergeDiff pMergeDiff, Observable<Optional<EditorKit>> pEditorKitObservable)
  {
    mergeDiff = pMergeDiff;
    editorPane = new JEditorPane();
    editorPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    editorKitDisposable = pEditorKitObservable.subscribe(pOptEditorKit -> pOptEditorKit.ifPresent(this::_setEditorKit));
    diffPane = new DiffPane(editorPane);
    mergeDiffDisposable = Observable.zip(
        mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges().getChangeChunks(),
        mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS).getFileChanges().getChangeChunks(), _ChangesEventPair::new)
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

  public JEditorPane getEditorPane()
  {
    return editorPane;
  }

  private void _setEditorKit(EditorKit pEditorKit)
  {
    // remove documentListener from the current document since the document could change with the new editorKit
    editorPane.getDocument().removeDocumentListener(paneDocumentListener);
    String currentText = editorPane.getText();
    editorPane.setEditorKit(pEditorKit);
    editorPane.setText(currentText);
    editorPane.setCaretPosition(0);
    // (re-) register documentListener
    editorPane.getDocument().addDocumentListener(paneDocumentListener);
  }

  private void _refreshContent(_ChangesEventPair pChangeChunkLists)
  {
    if (pChangeChunkLists.doUpdate)
    {
      paneDocumentListener.disable();
      // OLD because the content of the ForkPointTextPane is the version of the forkPoint (i.e. the old version in all cases since forkPoint
      // predates both commits)
      TextHighlightUtil.insertColoredText(editorPane,
                                          pChangeChunkLists.yourVersion,
                                          pChangeChunkLists.theirVersion,
                                          EChangeSide.OLD);
      editorPane.revalidate();
      editorPane.repaint();
      paneDocumentListener.enable();
    }
  }

  @Override
  public void discard()
  {
    mergeDiffDisposable.dispose();
    editorKitDisposable.dispose();
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

  private static class _ChangesEventPair
  {
    IFileChangesEvent yourVersion;
    IFileChangesEvent theirVersion;
    boolean doUpdate;

    _ChangesEventPair(IFileChangesEvent yourVersion, IFileChangesEvent theirVersion)
    {
      this.yourVersion = yourVersion;
      this.theirVersion = theirVersion;
      doUpdate = yourVersion.isUpdateUI() && theirVersion.isUpdateUI();
    }
  }
}
