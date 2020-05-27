package de.adito.git.nbm.sidebar;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IChangeDelta;
import de.adito.git.gui.rxjava.ViewPortSizeObservable;
import de.adito.git.gui.swing.LineNumber;
import de.adito.git.gui.swing.TextPaneUtil;
import de.adito.git.impl.observables.DocumentChangeObservable;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.actions.ShowAnnotationNBAction;
import de.adito.git.nbm.icon.NBIconLoader;
import de.adito.git.nbm.util.DocumentObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;
import org.openide.loaders.DataObject;
import org.openide.windows.WindowManager;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
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
  private static final int COLORIZER_WIDTH = 10;
  private static final int THROTTLE_LATEST_TIMER = 500;
  private final Observable<Optional<IRepository>> repository;
  private final JTextComponent targetEditor;
  private Observable<List<IChangeDelta>> chunkObservable;
  private final JViewport editorViewPort;
  private final CompositeDisposable disposable = new CompositeDisposable();
  private final File file;
  private final ImageIcon rightArrow = new NBIconLoader().getIcon(ARROW_RIGHT);
  private List<_ChangeHolder> changeList = new ArrayList<>();
  private Observable<List<_ChangeHolder>> rectanglesObs;
  private BufferedImage cachedImage;
  private _ChunkPopupMouseListener chunkPopupMouseListener;

  /**
   * A JPanel to show all the git changes in the editor
   *
   * @param pRepository the actual repository
   * @param pTarget     the text component of the editor
   */
  EditorColorizer(Observable<Optional<IRepository>> pRepository, DataObject pDataObject, JTextComponent pTarget)
  {
    repository = pRepository;
    targetEditor = pTarget;
    editorViewPort = _getJScrollPane(targetEditor).getViewport();
    setMinimumSize(new Dimension(COLORIZER_WIDTH, 0));
    setPreferredSize(new Dimension(COLORIZER_WIDTH, 0));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setLocation(0, 0);
    addPropertyChangeListener(evt -> _ancestorChanged(pRepository, evt));
    file = new File(pDataObject.getPrimaryFile().toURI());
  }

  private void _ancestorChanged(Observable<Optional<IRepository>> pRepository, PropertyChangeEvent evt)
  {
    if ("ancestor".equals(evt.getPropertyName()))
    {
      if (evt.getNewValue() == null)
      {
        discard();
      }
      else if (evt.getOldValue() == null)
      {
        _buildObservables();
        targetEditor.putClientProperty(IGitConstants.CHANGES_LOCATIONS_OBSERVABLE, rectanglesObs);
        if (chunkPopupMouseListener == null)
        {
          chunkPopupMouseListener = new _ChunkPopupMouseListener(pRepository, targetEditor);
          addMouseListener(chunkPopupMouseListener);
        }
      }
    }
  }

  private void _buildObservables()
  {
    Observable<String> actualText = Observable.create(new DocumentChangeObservable(targetEditor))
        .startWith(targetEditor.getDocument())
        .switchMap(DocumentObservable::create);
    // An observable that only triggers if the viewPort changes its size (not if it moves)
    Observable<Dimension> viewPortSizeObs = Observable.create(new ViewPortSizeObservable(editorViewPort));

    Observable<Optional<IRepositoryState>> repoState = repository
        .switchMap(pOptRepo -> pOptRepo.map(IRepository::getRepositoryState).orElse(Observable.just(Optional.empty())));
    // pass the repositoryState here because otherwise the Observable does not notice commits (the contents of the file do not change, but the chunks have to be updated
    // nevertheless)
    chunkObservable = Observable
        .combineLatest(repository, repoState, actualText.debounce(THROTTLE_LATEST_TIMER, TimeUnit.MILLISECONDS), (pRepoOpt, pRepoState, pText) -> {
          if (pRepoOpt.isPresent())
          {
            try
            {
              IRepository repo = pRepoOpt.get();
              EChangeType changeType = repo.getStatusOfSingleFile(file).getChangeType();
              // No changes if added or new, because the file can not be diffed -> not in index
              if (changeType == EChangeType.NEW || changeType == EChangeType.ADD)
                return new ArrayList<IChangeDelta>();
              return repo.diff(pText, file);
            }
            catch (Exception pE)
            {
              // do nothing on error, the EditorColorizer should just show nothing in that case
            }
          }
          return new ArrayList<IChangeDelta>();
        })
        .replay(1)
        .autoConnect(0, disposable::add)
        .distinctUntilChanged()
        .observeOn(Schedulers.computation());

    rectanglesObs = Observable.combineLatest(chunkObservable, viewPortSizeObs, (pChunks, pScroll) -> pChunks)
        .map(chunkList -> _calculateRectangles(targetEditor, chunkList));

    disposable.add(rectanglesObs
        .subscribe(pChangeList -> {
          changeList = pChangeList;
          cachedImage = _createBufferedImage(changeList, targetEditor.getHeight());
          repaint();
        }));
  }

  /**
   * @param pChangeList List of _ChangeHolders that should be drawn in the bufferedImage
   * @param pHeight     height of the targetPanel and thus the height of the bufferedImage
   * @return BufferedImage containing all the _ChangeHolders, background trasnparent
   */
  @NotNull
  private BufferedImage _createBufferedImage(@NotNull List<_ChangeHolder> pChangeList, int pHeight)
  {
    BufferedImage image = new BufferedImage(COLORIZER_WIDTH, Math.max(1, pHeight), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = (Graphics2D) image.getGraphics();
    pChangeList.forEach(change -> {
      if (change.changeChunk.getChangeType() == EChangeType.DELETE)
      {
        int y = change.rectangle.y;
        g.drawImage(rightArrow.getImage(), 0, y, COLORIZER_WIDTH, y + COLORIZER_WIDTH, 0, 0, rightArrow.getIconWidth(), rightArrow.getIconHeight(), null);
      }
      else
      {
        g.setColor(change.color);
        g.fill(change.rectangle);
      }
    });
    return image;
  }

  /**
   * Shows a popup via the way they are normally shown: By sending a mouseEvent
   *
   * @param pChangeDelta for which a ChunkWindowPopup should be shown
   */
  void showPopupForDelta(IChangeDelta pChangeDelta) throws BadLocationException
  {
    if (pChangeDelta != null)
    {
      int offset = pChangeDelta.getStartTextIndex(EChangeSide.NEW);
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


  @NotNull
  private List<_ChangeHolder> _calculateRectangles(JTextComponent pTarget, List<IChangeDelta> pDeltaList)
  {
    List<EditorColorizer._ChangeHolder> newChangeList = new ArrayList<>();
    try
    {
      LineNumber[] lineNumberPositions = TextPaneUtil.calculateLineYPositions(pTarget, pTarget.getUI().getRootView(pTarget));
      for (IChangeDelta chunk : pDeltaList)
      {
        _ChangeHolder changeHolder = _calculateRec(pTarget, chunk, lineNumberPositions);
        newChangeList.add(changeHolder);
      }
    }
    catch (BadLocationException pE)
    {
      throw new RuntimeException(pE);
    }
    return newChangeList;
  }

  /**
   * @param pTarget              The text component of the editor
   * @param pChange              A chunk of a file that was changed
   * @param pLineNumberPositions Array with LineNumbers, giving the y Location of each line
   */
  @NotNull
  private _ChangeHolder _calculateRec(JTextComponent pTarget, IChangeDelta pChange, LineNumber[] pLineNumberPositions)
  {
    int startLine = 0;
    int endLine = 0;
    switch (pChange.getChangeType())
    {
      case MODIFY:
      case ADD:
        startLine = pChange.getStartLine(EChangeSide.NEW);
        endLine = pChange.getEndLine(EChangeSide.NEW);
        break;
      case DELETE:
        startLine = pChange.getEndLine(EChangeSide.NEW);
        endLine = pChange.getEndLine(EChangeSide.NEW);
        break;
      default:
        break;
    }
    int startIndex = Math.min(pLineNumberPositions.length - 1, startLine);
    int endIndex = Math.min(pLineNumberPositions.length - 1, endLine);
    int height = pLineNumberPositions[endIndex].getYCoordinate() - pLineNumberPositions[startIndex].getYCoordinate();
    Rectangle changeRectangle = new Rectangle(0, pLineNumberPositions[startIndex].getYCoordinate(), COLORIZER_WIDTH, Math.max(height, pTarget.getFont().getSize()));
    return new _ChangeHolder(changeRectangle, pChange);
  }

  /**
   * @param pTarget The JTextComponent of the editor
   * @return The JScrollPane of the editor
   */
  static JScrollPane _getJScrollPane(JTextComponent pTarget)
  {
    Container parent = pTarget.getParent();
    while (!(parent instanceof JScrollPane))
    {
      parent = parent.getParent();
    }
    return (JScrollPane) parent;
  }

  static class _ChangeHolder
  {
    final Rectangle rectangle;
    final Color color;
    final IChangeDelta changeChunk;

    _ChangeHolder(Rectangle pRectangle, IChangeDelta pChangeDelta)
    {
      rectangle = pRectangle;
      color = pChangeDelta.getChangeType().getDiffColor();
      changeChunk = pChangeDelta;
    }
  }

  @Override
  protected void paintComponent(Graphics pG)
  {
    super.paintComponent(pG);
    if (cachedImage != null)
      _drawImage(pG, cachedImage);
  }

  /**
   * @param pG             Graphics object used to draw the image
   * @param pBufferedImage the bufferedImage to draw
   */
  private void _drawImage(Graphics pG, BufferedImage pBufferedImage)
  {
    int yCoordinate = editorViewPort.getViewRect().y;
    int yCoordinate2 = yCoordinate + targetEditor.getHeight();
    pG.drawImage(pBufferedImage, 0, yCoordinate, COLORIZER_WIDTH, yCoordinate2,
                 0, yCoordinate, COLORIZER_WIDTH, yCoordinate2, null);
  }

  @Override
  public void discard()
  {
    disposable.clear();
    targetEditor.putClientProperty(IGitConstants.CHANGES_LOCATIONS_OBSERVABLE, null);
    if (chunkPopupMouseListener != null)
      removeMouseListener(chunkPopupMouseListener);
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
    public void mousePressed(MouseEvent pEvent)
    {
      if (pEvent.isPopupTrigger())
      {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new ShowAnnotationNBAction(targetEditor));
        popupMenu.show(EditorColorizer.this, pEvent.getX(), pEvent.getY());
      }
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