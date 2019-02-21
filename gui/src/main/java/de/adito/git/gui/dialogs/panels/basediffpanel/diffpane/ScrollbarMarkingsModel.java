package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Model that keeps the ScrollbarMarkings of a MarkedScrollbar up to date
 *
 * @author m.kaspera, 25.01.2019
 */
public class ScrollbarMarkingsModel implements IDiscardable
{

  private final Disposable disposable;

  public ScrollbarMarkingsModel(@NotNull DiffPanelModel pModel, @NotNull JEditorPane pEditorPane, @NotNull MarkedScrollbar pMarkedScrollbar)
  {
    disposable = pModel.getFileChangesObservable().subscribe(
        pFileChangesEvent -> SwingUtilities.invokeLater(() -> pMarkedScrollbar.setMarkings(_getMarkings(pModel, pFileChangesEvent, pEditorPane))));
    pMarkedScrollbar.addComponentListener(new _ScrollBarResizeListener(pModel, pEditorPane, pMarkedScrollbar));
  }

  /**
   * @param pModel            DiffPanelModel that contains Functions to get the start/endLine of a IFileChangeChunk
   * @param pFileChangesEvent IFileChangesEvent that has the latest IFileChangeChunks
   * @param pEditorPane       JEditorPane displaying the IFileChangeChunks
   * @return List with ScrollbarMarkings, indicating at which position in the view Coordinate system the areas to mark are
   */
  @NotNull
  private List<ScrollbarMarking> _getMarkings(@NotNull DiffPanelModel pModel, @NotNull IFileChangesEvent pFileChangesEvent,
                                              @NotNull JEditorPane pEditorPane)
  {
    List<ScrollbarMarking> markings = new ArrayList<>();
    View view = pEditorPane.getUI().getRootView(pEditorPane);
    Element rootElement = pEditorPane.getDocument().getDefaultRootElement();
    // there can be parity lines in the Chunks, so we cannot use the start/endLine of the chunk directly. Instead we have to keep track of the lines
    int startLine = 0;
    for (IFileChangeChunk changeChunk : pFileChangesEvent.getNewValue())
    {
      int numLines = (pModel.getGetEndLine()
          .apply(changeChunk) - pModel.getGetStartLine().apply(changeChunk)) + pModel.getGetParityLines().apply(changeChunk).length();
      if (changeChunk.getChangeType() != EChangeType.SAME)
      {
        int startOffset = rootElement.getElement(startLine).getStartOffset();
        int endOffset = rootElement.getElement(startLine + numLines).getEndOffset();
        try
        {
          Rectangle bounds = view.modelToView(startOffset, Position.Bias.Forward, endOffset, Position.Bias.Backward, new Rectangle()).getBounds();
          markings.add(new ScrollbarMarking(bounds.y, bounds.height, changeChunk.getChangeType().getDiffColor()));
        }
        catch (BadLocationException pE)
        {
          throw new RuntimeException(pE);
        }
      }
      startLine += numLines;
    }
    return markings;
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }

  /**
   * ComponentAdapter listening for changes to the size of the Scrollbar and updating the markings
   */
  private class _ScrollBarResizeListener extends ComponentAdapter
  {

    private final MarkedScrollbar markedScrollbar;
    private final DiffPanelModel model;
    private final JEditorPane editorPane;

    _ScrollBarResizeListener(DiffPanelModel pModel, JEditorPane pEditorPane, MarkedScrollbar pMarkedScrollbar)
    {
      markedScrollbar = pMarkedScrollbar;
      model = pModel;
      editorPane = pEditorPane;
    }

    @Override
    public void componentResized(ComponentEvent pEvent)
    {
      markedScrollbar.setMarkings(_getMarkings(model, model.getFileChangesObservable().blockingFirst(), editorPane));
    }
  }
}
