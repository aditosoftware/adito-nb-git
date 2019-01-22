package de.adito.git.nbm.sidebar;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.icon.SwingIconLoaderImpl;
import de.adito.git.nbm.util.DocumentObservable;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.openide.loaders.DataObject;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static de.adito.git.gui.Constants.ARROW_RIGHT;

/**
 * @author a.arnold, 26.11.2018
 */
class EditorColorizer extends JPanel implements IDiscardable
{
  private Disposable disposable;
  private File file;
  private ImageIcon rightArrow = new SwingIconLoaderImpl().getIcon(ARROW_RIGHT);
  private List<_ChangeHolder> changeList = new ArrayList();

  /**
   * A JPanel to show all the git changes in the editor
   *
   * @param pRepository the actual repository
   * @param pTarget     the text component of the editor
   */
  EditorColorizer(Observable<Optional<IRepository>> pRepository, DataObject pDataObject, JTextComponent pTarget)
  {
    setMinimumSize(new Dimension(10, 0));
    setPreferredSize(new Dimension(10, 0));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setLocation(0, 0);

    Observable<String> actualText = DocumentObservable.create(pTarget.getDocument());
    file = new File(pDataObject.getPrimaryFile().toURI());

    /*
     * An observable to check the values on the ScrollPane.
     * this is important for the rectangles inside the editor bar.
     * The rectangles will be only rendered if the clipping is shown.
     */
    Observable<Integer> scrollObservable = Observable.create(new AbstractListenerObservable<AdjustmentListener, JTextComponent, Integer>(pTarget)
    {
      JScrollPane jScrollPane = _getJScollPane(pTarget);


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


    Observable<List<IFileChangeChunk>> chunkObservable = Observable
        .combineLatest(pRepository, actualText.debounce(0, TimeUnit.MILLISECONDS), (pRepoOpt, pText) -> {
          if (pRepoOpt.isPresent())
          {
            IRepository repo = pRepoOpt.get();
            EChangeType changeType = repo.getStatusOfSingleFile(file).getChangeType();
            // No changes if added or new, because the file can not be diffed -> not in index
            if (changeType == EChangeType.NEW || changeType == EChangeType.ADD)
              return List.of();
            return repo.diff(pText, file);
          }
          return List.of();
        });

    disposable = Observable.combineLatest(chunkObservable, scrollObservable, (pChunks, pScroll) -> pChunks)
        .subscribe(chunkList -> {
          changeList.clear();
          Rectangle scrollPaneRectangle = _getJScollPane(pTarget).getViewport().getViewRect();

          try
          {
            for (IFileChangeChunk chunk : chunkList)
              _calculateRec(pTarget, chunk, scrollPaneRectangle);
          }
          catch (Throwable e) // Catch Throwables because of NetBeans Execution Exceptions, if this Method is called too often
          {
            // nothing
          }

          repaint();
        });
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
        startLine = pChange.getBStart();
        endLine = pChange.getBEnd() - 1;
        break;
      case DELETE:
        startLine = pChange.getBEnd();
        endLine = pChange.getBEnd();
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
        changeList.add(new _ChangeHolder(changeRectangle, pChange.getChangeType()));
      }
    }
  }

  /**
   * @param pTarget The JTextComponent of the editor
   * @return The JScrollPane of the editor
   */
  private JScrollPane _getJScollPane(JTextComponent pTarget)
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
    final EChangeType changeType;

    _ChangeHolder(Rectangle pRectangle, EChangeType pChangeType)
    {
      rectangle = pRectangle;
      color = pChangeType.getDiffColor();
      changeType = pChangeType;
    }
  }

  @Override
  protected void paintComponent(Graphics pG)
  {
    super.paintComponent(pG);
    Graphics2D g = (Graphics2D) pG;
    changeList.forEach(change -> {
      if (change.changeType == EChangeType.DELETE)
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
}