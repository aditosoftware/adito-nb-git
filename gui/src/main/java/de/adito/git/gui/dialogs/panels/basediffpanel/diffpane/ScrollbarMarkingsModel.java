package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.EChangeStatus;
import de.adito.git.api.data.diff.IChangeDelta;
import de.adito.git.api.data.diff.IFileDiff;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.View;
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
        pFileChangesEvent -> SwingUtilities.invokeLater(() -> pMarkedScrollbar.setMarkings(_getMarkings(pModel, pFileChangesEvent.getFileDiff(), pEditorPane))));
    pMarkedScrollbar.addComponentListener(new _ScrollBarResizeListener(pModel, pEditorPane, pMarkedScrollbar));
  }

  /**
   * @param pModel      DiffPanelModel that contains Functions to get the start/endLine of a IFileChangeChunk
   * @param pEditorPane JEditorPane displaying the IFileChangeChunks
   * @return List with ScrollbarMarkings, indicating at which position in the view Coordinate system the areas to mark are
   */
  @NotNull
  private List<ScrollbarMarking> _getMarkings(@NotNull DiffPanelModel pModel, @Nullable IFileDiff pFileDiff, @NotNull JEditorPane pEditorPane)
  {
    List<ScrollbarMarking> markings = new ArrayList<>();
    View view = pEditorPane.getUI().getRootView(pEditorPane);
    // there can be parity lines in the Chunks, so we cannot use the start/endLine of the chunk directly. Instead we have to keep track of the lines
    for (IChangeDelta changeDelta : pFileDiff == null ? List.<IChangeDelta>of() : pFileDiff.getChangeDeltas())
    {
      if (changeDelta.getChangeStatus().getChangeStatus() == EChangeStatus.PENDING)
      {
        int startOffset = changeDelta.getStartTextIndex(pModel.getChangeSide());
        int endOffset = changeDelta.getEndTextIndex(pModel.getChangeSide());
        try
        {
          Rectangle bounds = view.modelToView(startOffset, Position.Bias.Forward, endOffset, Position.Bias.Backward, new Rectangle()).getBounds();
          markings.add(new ScrollbarMarking(bounds.y, bounds.height, changeDelta.getChangeStatus().getChangeType().getDiffColor()));
        }
        catch (BadLocationException pE)
        {
          throw new RuntimeException(pE);
        }
      }
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
      markedScrollbar.setMarkings(_getMarkings(model, model.getFileChangesObservable().blockingFirst().getFileDiff(), editorPane));
    }
  }
}
