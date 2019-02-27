package de.adito.git.nbm.sidebar;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.icon.SwingIconLoaderImpl;
import de.adito.git.nbm.actions.ShowAnnotationNBAction;
import de.adito.git.nbm.util.DocumentObservable;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;
import org.openide.loaders.DataObject;
import org.openide.windows.WindowManager;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static de.adito.git.gui.Constants.ARROW_RIGHT;

/**
 * @author a.arnold, 26.11.2018
 */
class EditorColorizer extends JPanel implements IDiscardable
{
  private final JTextComponent targetEditor;
  private final Observable<List<IFileChangeChunk>> chunkObservable;
  private Disposable disposable;
  private File file;
  private ImageIcon rightArrow = new SwingIconLoaderImpl().getIcon(ARROW_RIGHT);
  private List<_ChangeHolder> changeList = new ArrayList<>();

  /**
   * A JPanel to show all the git changes in the editor
   *
   * @param pRepository the actual repository
   * @param pTarget     the text component of the editor
   */
  EditorColorizer(Observable<Optional<IRepository>> pRepository, DataObject pDataObject, JTextComponent pTarget)
  {
    targetEditor = pTarget;
    setMinimumSize(new Dimension(10, 0));
    setPreferredSize(new Dimension(10, 0));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setLocation(0, 0);

    Observable<String> actualText = DocumentObservable.create(targetEditor.getDocument());
    file = new File(pDataObject.getPrimaryFile().toURI());
    addMouseListener(new _ChunkPopupMouseListener(pRepository, targetEditor));

    /*
     * An observable to check the values on the ScrollPane.
     * this is important for the rectangles inside the editor bar.
     * The rectangles will be only rendered if the clipping is shown.
     */
    Observable<Integer> scrollObservable = Observable.create(new AbstractListenerObservable<AdjustmentListener, JTextComponent, Integer>(targetEditor)
    {
      JScrollPane jScrollPane = _getJScrollPane(targetEditor);


      @NotNull
      @Override
      protected AdjustmentListener registerListener(@NotNull JTextComponent pListenableValue, @NotNull IFireable<Integer> pFireable)
      {
        AdjustmentListener adjustmentListener = e -> pFireable.fireValueChanged(e.getValue());
        jScrollPane.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);
        return adjustmentListener;
      }

      @Override
      protected void removeListener(@NotNull JTextComponent pListenableValue, @NotNull AdjustmentListener pAdjustmentListener)
      {
        jScrollPane.getVerticalScrollBar().removeAdjustmentListener(pAdjustmentListener);
      }
    });


    chunkObservable = Observable
        .combineLatest(pRepository, actualText.debounce(0, TimeUnit.MILLISECONDS), (pRepoOpt, pText) -> {
          if (pRepoOpt.isPresent())
          {
            IRepository repo = pRepoOpt.get();
            EChangeType changeType = repo.getStatusOfSingleFile(file).getChangeType();
            // No changes if added or new, because the file can not be diffed -> not in index
            if (changeType == EChangeType.NEW || changeType == EChangeType.ADD)
              return new ArrayList<IFileChangeChunk>();
            return repo.diff(pText, file);
          }
          return new ArrayList<IFileChangeChunk>();
        })
        .share()
        .subscribeWith(BehaviorSubject.create());

    disposable = Observable.combineLatest(chunkObservable, scrollObservable, (pChunks, pScroll) -> pChunks)
        .subscribe(chunkList -> {
          changeList.clear();
          Rectangle scrollPaneRectangle = _getJScrollPane(targetEditor).getViewport().getViewRect();

          try
          {
            for (IFileChangeChunk chunk : chunkList)
              _calculateRec(targetEditor, chunk, scrollPaneRectangle);
          }
          catch (Throwable e) // Catch Throwables because of NetBeans Execution Exceptions, if this Method is called too often
          {
            // nothing
          }

          repaint();
        });
  }

  /**
   * Shows a popup via the way they are normally shown: By sending a mouseEvent
   *
   * @param pChangeChunk for which a ChunkWindowPopup should be shown
   */
  void showPopupForChunk(IFileChangeChunk pChangeChunk) throws BadLocationException
  {
    if (pChangeChunk != null)
    {
      int offset = targetEditor.getDocument().getDefaultRootElement().getElement(pChangeChunk.getStart(EChangeSide.NEW)).getStartOffset();
      MouseEvent mouseEvent = new MouseEvent(this, 0, System.currentTimeMillis(), InputEvent.BUTTON1_DOWN_MASK, 0,
                                             targetEditor.getUI().getRootView(targetEditor)
                                                 .modelToView(offset, new Rectangle(), Position.Bias.Forward)
                                                 .getBounds().y,
                                             1, false, MouseEvent.BUTTON1);
      for (MouseListener listener : this.getMouseListeners())
      {
        listener.mouseReleased(mouseEvent);
      }
    }
  }


  /**
   * @param pTarget              The text component of the editor
   * @param pChange              A chunk of a file that was changed
   * @param pScrollPaneRectangle The rectangle of the viewport of the JScrollPane
   */
  private void _calculateRec(JTextComponent pTarget, IFileChangeChunk pChange, Rectangle pScrollPaneRectangle) throws BadLocationException
  {
    int startLine = 0;
    int endLine = 0;
    switch (pChange.getChangeType())
    {
      case MODIFY:
      case ADD:
        startLine = pChange.getStart(EChangeSide.NEW);
        endLine = pChange.getEnd(EChangeSide.NEW) - 1;
        break;
      case DELETE:
        startLine = pChange.getEnd(EChangeSide.NEW);
        endLine = pChange.getEnd(EChangeSide.NEW);
        break;
      default:
        break;
    }
    Element startElement = pTarget.getDocument().getDefaultRootElement().getElement(startLine);
    Element endElement = pTarget.getDocument().getDefaultRootElement().getElement(endLine);
    int endOffset = endElement.getStartOffset();
    int startOffset = startElement.getStartOffset();

    if (pChange.getChangeType().equals(EChangeType.MODIFY))
    {
      endOffset = endElement.getEndOffset() - 1;
    }

    View view = pTarget.getUI().getRootView(pTarget);
    if (view != null)
    {
      Rectangle changeRectangle =
          view.modelToView(startOffset, Position.Bias.Forward, endOffset, Position.Bias.Forward, new Rectangle()).getBounds();

      if (pScrollPaneRectangle.intersects(changeRectangle))
      {
        changeList.add(new _ChangeHolder(changeRectangle, pChange));
      }
    }
  }

  /**
   * @param pTarget The JTextComponent of the editor
   * @return The JScrollPane of the editor
   */
  private JScrollPane _getJScrollPane(JTextComponent pTarget)
  {
    Container parent = pTarget.getParent();
    while (!(parent instanceof JScrollPane))
    {
      parent = parent.getParent();
    }
    return (JScrollPane) parent;
  }

  private class _ChangeHolder
  {
    final Rectangle rectangle;
    final Color color;
    final IFileChangeChunk changeChunk;

    _ChangeHolder(Rectangle pRectangle, IFileChangeChunk pChangeChunk)
    {
      rectangle = pRectangle;
      color = pChangeChunk.getChangeType().getDiffColor();
      changeChunk = pChangeChunk;
    }
  }

  @Override
  protected void paintComponent(Graphics pG)
  {
    super.paintComponent(pG);
    Graphics2D g = (Graphics2D) pG;
    changeList.forEach(change -> {
      if (change.changeChunk.getChangeType() == EChangeType.DELETE)
      {
        int y = change.rectangle.y;
        g.drawImage(rightArrow.getImage(), 0, y, null);
      }
      else
      {
        g.setColor(change.color);
        g.fill(change.rectangle);
      }
    });
  }

  @Override
  public void discard()
  {
    if (disposable != null && !disposable.isDisposed())
    {
      disposable.dispose();
      disposable = null;
    }
  }

  /**
   * MouseListener that displays a ChunkPopupWindow if the mouse is pressed over one of the ChangeChunks in the EditorColorizer
   */
  private class _ChunkPopupMouseListener extends MouseAdapter
  {

    private final Observable<Optional<IRepository>> repository;
    private final JTextComponent target;

    _ChunkPopupMouseListener(Observable<Optional<IRepository>> pRepository, JTextComponent pTarget)
    {
      repository = pRepository;
      target = pTarget;
    }

    @Override
    public void mouseReleased(MouseEvent pEvent)
    {
      if (SwingUtilities.isLeftMouseButton(pEvent))
      {
        Point point = pEvent.getPoint();
        for (_ChangeHolder changeHolder : changeList)
        {
          if (changeHolder.rectangle.contains(point))
          {
            Point locationOnScreen = pEvent.getLocationOnScreen();
            locationOnScreen.y = locationOnScreen.y + (changeHolder.rectangle.y + changeHolder.rectangle.height - point.y);
            ChunkPopupWindow menu = new ChunkPopupWindow(repository, WindowManager.getDefault().getMainWindow(),
                                                         locationOnScreen, changeHolder.changeChunk, chunkObservable, target,
                                                         EditorColorizer.this, file);
            menu.setVisible(true);
          }
        }
      }
      else if (pEvent.isPopupTrigger())
      {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new ShowAnnotationNBAction(targetEditor));
        popupMenu.show(EditorColorizer.this, pEvent.getX(), pEvent.getY());
      }
    }
  }
}