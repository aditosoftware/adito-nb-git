package de.adito.git.nbm.sidebar;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.IDiscardable;
import de.adito.git.nbm.util.DocumentObservable;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import org.jetbrains.annotations.*;
import org.openide.loaders.DataObject;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.text.*;
import java.util.List;
import java.util.*;


/**
 * This class annotates all lines of code with the author of the lines.
 * Every single line gets the author and the date of commit.
 *
 * @author a.arnold, 22.01.2019
 */
public class Annotator extends JPanel implements IDiscardable
{
  private static final String NOT_COMMITTED_YET ="Not Committed Yet";
  private static final int FREE_SPACE = 6; // have to be modulo 2
  private static final Color _FG_COLOR = new JTextField().getForeground();
  private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
  private final JTextComponent target;
  private final List<_LineHolder> annotationList = new ArrayList<>();
  private final CompositeDisposable disposables = new CompositeDisposable();

  Annotator(Observable<Optional<IRepository>> pRepository, DataObject pDataObject, JTextComponent pTarget)
  {
    target = pTarget;
    setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setLocation(0, 0);
    File file = new File(pDataObject.getPrimaryFile().toURI());
    Observable<String> actualText = DocumentObservable.create(target.getDocument());

    // Observable to check the File changes between the last pulled file and the actual file
    Observable<List<IFileChangeChunk>> chunkObservable = Observable
        .combineLatest(pRepository, actualText, (pRepoOpt, pText) -> {
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

    Observable<Optional<IBlame>> blameObservable = pRepository
        .switchMap(optRepo -> optRepo.map(pRepo -> pRepo.getBlame(file)).orElse(Observable.just(Optional.<IBlame>empty())));

    disposables.add(
        Observable.combineLatest(blameObservable, chunkObservable,
                                 Observable.create(new _ScrollObservable(target)), (pBlame, pChunks, pScrollValue) -> {
              if (!pBlame.isPresent())
                return List.<_LineHolder>of();
              List<String> stringList;
              Rectangle scrollPaneRectangle = _getJScollPane(target).getViewport().getViewRect();
              stringList = _calculateStringList(pBlame.get(), pChunks);
              List<_LineHolder> holders = new ArrayList<>();
              try
              {
                for (int i = 0; i < stringList.size(); i++)
                {
                  _LineHolder holder = _calculateRec(scrollPaneRectangle, stringList.get(i), i);
                  if (holder != null)
                    holders.add(holder);
                }
              }
              catch (Throwable e)
              {
                // Catch Throwables because of NetBeans Execution Exceptions, if this Method is called too often
              }
              return holders;
            })
            .subscribe(pHolders -> {
              annotationList.clear();
              annotationList.addAll(pHolders);

              // repaint
              SwingUtilities.invokeLater(() -> {
                revalidate();
                repaint();
              });
            }));

  }

  /**
   * Calculate the list of Strings to print in each line as annotations
   *
   * @param pBlame  the "blame" annotation of the file
   * @param pChunks the chunks of the file
   * @return Returns a list which contains the annotations (with the new lines which has no annotations)
   */
  private List<String> _calculateStringList(IBlame pBlame, List<IFileChangeChunk> pChunks)
  {
    List<String> list = new ArrayList<>();
    for (int i = 0; i < pBlame.getLineCount(); i++)
    {
      String timeString = dateFormat.format(pBlame.getTimeStamp(i));
      list.add(timeString + " - " + pBlame.getSourceAuthor(i));
    }

    /*
    get the longest name to set the wight to the panel
     */
    String longestName = "";
    for (String name : list)
    {
      if (name.length() > longestName.length())
      {
        longestName = name;
      }
    }
    setPreferredSize(new Dimension(getFontMetrics(target.getFont()).stringWidth(longestName) + FREE_SPACE, 0));

    //get all chunks to check the type
    for (IFileChangeChunk chunk : pChunks)
    {
      if (chunk.getChangeType() == EChangeType.ADD)
      {
        _addLines(chunk, list);
      }
      if (chunk.getChangeType() == EChangeType.DELETE)
      {
        _deleteLines(chunk, list);
      }
      if (chunk.getChangeType() == EChangeType.MODIFY)
      {
        _deleteLines(chunk, list);
        _addLines(chunk, list);
      }
    }
    return list;
  }

  /**
   * Add a new Line to the annotator
   *
   * @param pChunk      the chunk to check the lines to add
   * @param pStringList the string list to change the content
   */
  private void _addLines(IFileChangeChunk pChunk, List<String> pStringList)
  {
    int linesToAdd = pChunk.getBEnd() - pChunk.getBStart() - 1;
    for (int i = 0; i <= linesToAdd; i++)
      pStringList.add(pChunk.getBStart(), "");
  }

  /**
   * Delete one line of the annotator
   *
   * @param pChunk      the chunk to check the lines to delete
   * @param pStringList the string list to change the content
   */
  private void _deleteLines(IFileChangeChunk pChunk, List<String> pStringList)
  {
    int linesToDelete = pChunk.getAEnd() - pChunk.getAStart();
    for (int i = 0; i < linesToDelete; i++)
      pStringList.remove(pChunk.getBStart() - 1);
  }

  /**
   * Calculates one single annotations, for one line. It check the intersects of the scrollRectangle
   * which is a ViewPort and save the contain one in a list
   *
   * @param pScrollPaneRectangle The Viewport on the TextPanel
   * @param pSourceAuthor        The author of the line of code
   * @param pLine                the line of the code
   * @return return a {@code _LineHolder} which combines the size and position of an annotation and the source author
   */
  @Nullable
  private _LineHolder _calculateRec(Rectangle pScrollPaneRectangle, String pSourceAuthor, int pLine)
  {
    //if the pLine is not commited return a empty pLine
    if (pSourceAuthor.equals(NOT_COMMITTED_YET)) //NOI18N
      pSourceAuthor = "";

    // get the offset to set the annotation at the right high and width
    Element lineElement = target.getDocument().getDefaultRootElement().getElement(pLine);
    int startOffset = lineElement.getStartOffset();
    int endOffset = lineElement.getEndOffset();
    View view = target.getUI().getRootView(target);

    if (view != null)
    {
      Rectangle changeRectangle;
      try
      {
        changeRectangle = view.modelToView(startOffset, Position.Bias.Forward, endOffset, Position.Bias.Backward, new Rectangle()).getBounds();
        changeRectangle.setSize(FREE_SPACE / 2, target.getFontMetrics(target.getFont()).getHeight());
      }
      catch (BadLocationException pE)
      {
        throw new RuntimeException("can't get the model of the rectangle");
      }

      if (pScrollPaneRectangle.intersects(changeRectangle))
      {
        return new _LineHolder(changeRectangle, pSourceAuthor);
      }
    }

    return null;
  }

  @Override
  protected void paintComponent(Graphics pG)
  {
    super.paintComponent(pG);
    int fontHeight = pG.getFontMetrics().getAscent();
    pG.setFont(target.getFont());
    ((Graphics2D) pG).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    pG.setColor(_FG_COLOR);
    for (_LineHolder lineHolder : annotationList)
    {
      Rectangle viewRect = lineHolder.viewRect;
      int x = viewRect.x;
      int y = viewRect.y + ((viewRect.height - fontHeight) / 2);
      pG.drawString(lineHolder.text, x, y + fontHeight);
    }
  }

  /**
   * A static class to save the single lines of annotations
   */
  private static class _LineHolder
  {
    private final Rectangle viewRect;
    private final String text;

    _LineHolder(Rectangle pViewRect, String pText)
    {
      viewRect = pViewRect;
      text = pText;
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

  @Override
  public void discard()
  {
    if (!disposables.isDisposed())
      disposables.dispose();
  }

  /*
   * An observable to check the values on the ScrollPane.
   * this is important for the rectangles inside the editor bar.
   * The rectangles will be only rendered if the clipping is shown.
   */
  private class _ScrollObservable extends AbstractListenerObservable<AdjustmentListener, JTextComponent, Integer>
  {
    private final JTextComponent target;
    private JScrollPane jScrollPane;

    _ScrollObservable(JTextComponent pTarget)
    {
      super(pTarget);
      target = pTarget;
      jScrollPane = _getJScollPane(target);
    }

    @NotNull
    @Override
    protected AdjustmentListener registerListener(@NotNull JTextComponent pTarget, @NotNull IFireable<Integer> pFireable)
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
  }
}
