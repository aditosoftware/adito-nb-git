package de.adito.git.gui.dialogs.panels.basediffpanel.textpanes;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.*;
import de.adito.git.gui.TextHighlightUtil;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import de.adito.git.gui.dialogs.panels.basediffpanel.IDiffPaneUtil;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.DiffPaneContainer;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.MarkedScrollbar;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.ScrollbarMarkingsModel;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Optional;

/**
 * Wrapper around a DiffPane, similar to ForkPointPaneWrapper. Made so that both use a DiffPane for displaying the LineNumPanels/ChoiceButtonPanels
 * while having different functionality in the central editorPane
 *
 * @author m.kaspera, 13.12.2018
 */
public class DiffPaneWrapper implements IDiscardable, IPaneWrapper
{

  private final JEditorPane editorPane;
  private final DiffPaneContainer diffPaneContainer;
  private final DiffPanelModel model;
  private final Disposable fileChangeDisposable;
  private final Disposable editorKitDisposable;
  private final ScrollbarMarkingsModel scrollbarMarkingsModel;
  private IFileDiff currentFileDiff;

  /**
   * @param pModel           DiffPanelModel that defines what is done when inserting text/how the LineNumbers are retrieved
   * @param pHeaderAlignment alignment of the header
   */
  public DiffPaneWrapper(@NotNull DiffPanelModel pModel, @Nullable String pHeader, int pHeaderAlignment, @NotNull Observable<Optional<EditorKit>> pEditorKitObservable)
  {
    model = pModel;
    editorPane = new JEditorPane();
    editorPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    editorPane.addFocusListener(new FocusListener()
    {

      @Override
      public void focusGained(FocusEvent pFocusEvent)
      {
        editorPane.getCaret().setVisible(true);
      }

      @Override
      public void focusLost(FocusEvent pFocusEvent)
      {
        editorPane.getCaret().setVisible(false);
      }
    });
    editorPane.setEditable(false);
    diffPaneContainer = new DiffPaneContainer(editorPane, pHeader, pHeaderAlignment);
    fileChangeDisposable = model.getFileChangesObservable()
        .subscribe(this::_fileDiffObservableUpdated);
    editorKitDisposable = pEditorKitObservable.subscribe(pOptEditorKit
                                                             -> pOptEditorKit.ifPresent(pEditorKit -> SwingUtilities.invokeLater(() -> _setEditorKit(pEditorKit))));
    MarkedScrollbar markedScrollbar = new MarkedScrollbar();
    getScrollPane().setVerticalScrollBar(markedScrollbar);
    scrollbarMarkingsModel = new ScrollbarMarkingsModel(pModel, editorPane, markedScrollbar);
  }

  /**
   * @return JScrollPane with the content of this DiffPane, add this to your panel
   */
  public JScrollPane getScrollPane()
  {
    return diffPaneContainer.getScrollPane();
  }

  /**
   * @return DiffPane that this wrapper is made for, only use this to add LineNumber/ChoiceButtonPanels. Add the JScrollPane via getScrollPane() to
   * the panel/component that should display the DiffPane
   */
  public DiffPaneContainer getPaneContainer()
  {
    return diffPaneContainer;
  }

  /**
   * @return the JEditorPane displaying the text for this DiffPaneWrapper
   */
  public JEditorPane getEditorPane()
  {
    return editorPane;
  }

  public boolean isEditorFocusOwner()
  {
    return editorPane.isFocusOwner();
  }

  /**
   * moves the caret to the position of the next changed chunk, as seen from the current position of the caret
   *
   * @param pTextPane textPane that displays the other side of the diff
   */
  public void moveCaretToNextChunk(JTextComponent pTextPane)
  {
    IChangeDelta nextChunk = IDiffPaneUtil.getNextDelta(editorPane, currentFileDiff.getChangeDeltas(), model.getChangeSide());
    _moveCaretToChunk(pTextPane, nextChunk);
  }

  /**
   * moves the caret to the position of the previous changed chunk, as seen from the current position of the caret
   *
   * @param pTextPane textPane that displays the other side of the diff
   */
  public void moveCaretToPreviousChunk(JTextComponent pTextPane)
  {
    IChangeDelta previousDelta = IDiffPaneUtil.getPreviousDelta(editorPane, currentFileDiff.getChangeDeltas(), model.getChangeSide());
    _moveCaretToChunk(pTextPane, previousDelta);
  }

  /**
   * moves the caret to the position of the given chunk
   *
   * @param pTextPane textPane that displays the other side of the diff
   * @param pChunk    chunk that the caret should be moved to
   */
  private void _moveCaretToChunk(JTextComponent pTextPane, IChangeDelta pChunk)
  {
    IDiffPaneUtil.moveCaretToDelta(editorPane, pChunk, model.getChangeSide());
    IDiffPaneUtil.moveCaretToDelta(pTextPane, pChunk, model.getChangeSide() == EChangeSide.NEW ? EChangeSide.OLD : EChangeSide.NEW);
    editorPane.requestFocus();
  }

  @Override
  public void discard()
  {
    diffPaneContainer.discard();
    fileChangeDisposable.dispose();
    editorKitDisposable.dispose();
    scrollbarMarkingsModel.discard();
  }

  private void _setEditorKit(EditorKit pEditorKit)
  {
    String currentText = editorPane.getText();
    editorPane.setEditorKit(pEditorKit);
    editorPane.setText(currentText);
    editorPane.setCaretPosition(0);
    getScrollPane().getVerticalScrollBar().setValue(0);
  }

  private void _fileDiffObservableUpdated(IDeltaTextChangeEvent pTextChangeEvent)
  {
    currentFileDiff = pTextChangeEvent.getFileDiff();
    _textChanged(pTextChangeEvent);
  }

  private void _textChanged(IDeltaTextChangeEvent pTextChangeEvent)
  {
    if (pTextChangeEvent.getSide() == model.getChangeSide() && pTextChangeEvent.isInit())
      editorPane.setText("");
    // insert the text from the event
    SwingUtilities.invokeLater(() -> {
      TextHighlightUtil.insertColoredText(editorPane,
                                          pTextChangeEvent,
                                          model.getChangeSide());
      editorPane.revalidate();
      editorPane.repaint();
    });
  }
}
