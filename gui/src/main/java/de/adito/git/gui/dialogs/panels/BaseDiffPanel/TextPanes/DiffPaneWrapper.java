package de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.TextHighlightUtil;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.DiffPanelModel;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.DiffPane.DiffPane;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.text.EditorKit;
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

  /**
   * @param pModel DiffPanelModel that defines what is done when inserting text/how the LineNumbers are retrieved
   */
  public DiffPaneWrapper(DiffPanelModel pModel, Observable<EditorKit> pEditorKitObservable)
  {
    model = pModel;
    editorPane = new JEditorPane();
    editorPane.setEditable(false);
    diffPane = new DiffPane(editorPane);
    fileChangeDisposable = model.getFileChangesObservable()
        .subscribe(pFileChangesEvent -> {
          if (pFileChangesEvent.isUpdateUI())
            _textChanged(pFileChangesEvent.getNewValue());
        });
    editorKitDisposable = pEditorKitObservable.subscribe(this::_setEditorKit);
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
    final int scrollBarPos = getScrollPane() != null ? getScrollPane().getVerticalScrollBar().getValue() : 0;
    // insert the text from the IFileDiffs
    TextHighlightUtil.insertColoredText(editorPane,
                                        pChangeChunkList,
                                        model.getGetLines(),
                                        model.getGetParityLines(),
                                        model.getGetStartLine(),
                                        model.getGetEndLine());
    editorPane.revalidate();
    SwingUtilities.invokeLater(() -> getScrollPane().getVerticalScrollBar().setValue(scrollBarPos));
  }
}
