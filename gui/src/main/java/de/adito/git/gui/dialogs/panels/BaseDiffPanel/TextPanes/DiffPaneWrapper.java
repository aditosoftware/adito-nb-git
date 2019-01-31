package de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.TextHighlightUtil;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.DiffPane.DiffPane;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.DiffPane.MarkedScrollbar;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.DiffPane.ScrollbarMarkingsModel;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.DiffPanelModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.Rectangle;
import java.util.List;

/**
 * Wrapper around a DiffPane, similar to ForkPointPaneWrapper. Made so that both use a DiffPane for displaying the LineNumPanels/ChoiceButtonPanels
 * while having different functionality in the central editorPane
 *
 * @author m.kaspera, 13.12.2018
 */
public class DiffPaneWrapper implements IDiscardable
{

  private final JEditorPane editorPane;
  private final DiffPane diffPane;
  private final DiffPanelModel model;
  private final Disposable fileChangeDisposable;
  private final Disposable editorKitDisposable;
  private final ScrollbarMarkingsModel scrollbarMarkingsModel;

  /**
   * @param pModel DiffPanelModel that defines what is done when inserting text/how the LineNumbers are retrieved
   */
  public DiffPaneWrapper(DiffPanelModel pModel, Observable<EditorKit> pEditorKitObservable)
  {
    model = pModel;
    editorPane = new JEditorPane();
    editorPane.setEditable(false);
    editorPane.setEnabled(false);
    diffPane = new DiffPane(editorPane);
    fileChangeDisposable = model.getFileChangesObservable()
        .subscribe(pFileChangesEvent -> {
          if (pFileChangesEvent.isUpdateUI())
            _textChanged(pFileChangesEvent.getNewValue());
        });
    editorKitDisposable = pEditorKitObservable.subscribe(pEditorKit -> SwingUtilities.invokeLater(() -> _setEditorKit(pEditorKit)));
    MarkedScrollbar markedScrollbar = new MarkedScrollbar();
    getScrollPane().setVerticalScrollBar(markedScrollbar);
    scrollbarMarkingsModel = new ScrollbarMarkingsModel(pModel, editorPane, markedScrollbar);
  }

  /**
   * @return JScrollPane with the content of this DiffPane, add this to your panel
   */
  public JScrollPane getScrollPane()
  {
    return diffPane.getScrollPane();
  }

  /**
   * @return DiffPane that this wrapper is made for, only use this to add LineNumber/ChoiceButtonPanels. Add the JScrollPane via getScrollPane() to
   * the panel/component that should display the DiffPane
   */
  public DiffPane getPane()
  {
    return diffPane;
  }

  @Override
  public void discard()
  {
    diffPane.discard();
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
  }

  private void _textChanged(List<IFileChangeChunk> pChangeChunkList)
  {
    final int caretPosition = editorPane.getCaretPosition();
    final Rectangle visibleRect = getScrollPane() != null ? getScrollPane().getVisibleRect() : new Rectangle();
    // insert the text from the IFileDiffs
    TextHighlightUtil.insertColoredText(editorPane,
                                        pChangeChunkList,
                                        model.getGetLines(),
                                        model.getGetParityLines(),
                                        model.getGetStartLine(),
                                        model.getGetEndLine());
    editorPane.revalidate();
    SwingUtilities.invokeLater(() -> {
      // For whatever reason the EditorCaret thinks it's a good idea to jump to the caret position on text change in a disabled EditorPane,
      // this at least doesn't jump the editor to the bottom of the editor each time and jumps back if the user remembers to set the caret
      editorPane.setCaretPosition(Math.min(caretPosition, editorPane.getDocument().getLength()));
      editorPane.scrollRectToVisible(visibleRect);
    });
  }
}
