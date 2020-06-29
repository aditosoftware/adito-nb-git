package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.*;
import de.adito.git.gui.dialogs.panels.basediffpanel.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.jetbrains.annotations.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

/**
 * Model that keeps the ScrollbarMarkings of a MarkedScrollbar up to date
 *
 * @author m.kaspera, 25.01.2019
 */
public class ScrollbarMarkingsModel implements IDiscardable
{

  private final CompositeDisposable disposable = new CompositeDisposable();

  public ScrollbarMarkingsModel(@NotNull DiffPanelModel pModel, @NotNull JEditorPane pEditorPane, @NotNull MarkedScrollbar pMarkedScrollbar)
  {
    // add a mouseFirstActionObservable so that when the first mouse action by the user happens, the heights are recalculated. This is done to make it more likely that
    // the layouting by the editorPane is done when the heights for changed parts are retrieved
    disposable.add(Observable.combineLatest(new MouseFirstActionObservableWrapper(pEditorPane).getObservable(), pModel.getFileChangesObservable(),
                                            (pObj, pFileChange) -> pFileChange)
                       .subscribe(pFileChangesEvent ->
                                      SwingUtilities.invokeLater(() -> _getMarkings(pModel, pFileChangesEvent.getFileDiff(), pEditorPane, pMarkedScrollbar))));
    new MouseFirstActionObservableWrapper(pEditorPane);
    pMarkedScrollbar.addComponentListener(new _ScrollBarResizeListener(pModel, pEditorPane, pMarkedScrollbar));
  }

  /**
   * sets the List with ScrollbarMarkings to pMarkedScrollbar, indicating at which position in the view Coordinate system the areas to mark are
   *
   * @param pModel      DiffPanelModel that contains Functions to get the start/endLine of a IFileChangeChunk
   * @param pEditorPane JEditorPane displaying the IFileChangeChunks
   */
  private void _getMarkings(@NotNull DiffPanelModel pModel, @Nullable IFileDiff pFileDiff, @NotNull JEditorPane pEditorPane, @NotNull MarkedScrollbar pMarkedScrollbar)
  {
    if (pFileDiff != null)
    {
      if (pEditorPane.getDocument().getLength() <= 0 && pFileDiff.getText(pModel.getChangeSide()).length() > 0)
      {
        SwingUtilities.invokeLater(() -> _getMarkings(pModel, pFileDiff, pEditorPane, pMarkedScrollbar));
        return;
      }
      List<ScrollbarMarking> markings = new ArrayList<>();
      View view = pEditorPane.getUI().getRootView(pEditorPane);
      // there can be parity lines in the Chunks, so we cannot use the start/endLine of the chunk directly. Instead we have to keep track of the lines
      for (IChangeDelta changeDelta : pFileDiff.getChangeDeltas())
      {
        if (changeDelta.getChangeStatus() == EChangeStatus.PENDING)
        {
          int startOffset = changeDelta.getStartTextIndex(pModel.getChangeSide());
          int endOffset = changeDelta.getEndTextIndex(pModel.getChangeSide());
          try
          {
            Rectangle bounds = view.modelToView(startOffset, Position.Bias.Forward, endOffset, Position.Bias.Backward, new Rectangle()).getBounds();
            markings.add(new ScrollbarMarking(bounds.y, bounds.height, changeDelta.getDiffColor()));
          }
          catch (BadLocationException pE)
          {
            throw new RuntimeException(pE);
          }
        }
      }
      pMarkedScrollbar.setMarkings(markings);
    }
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
      _getMarkings(model, model.getFileChangesObservable().blockingFirst().getFileDiff(), editorPane, markedScrollbar);
    }
  }
}
