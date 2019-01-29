package de.adito.git.nbm.sidebar;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IBlame;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.gui.IDiscardable;
import de.adito.git.nbm.util.DocumentObservable;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.openide.loaders.DataObject;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * This class annotates all lines of code with the author of the lines.
 * Every single line gets the author and the date of commit.
 *
 * @author a.arnold, 22.01.2019
 */
public class Annotator extends JPanel implements IDiscardable
{
  private static final String NOT_COMMITTED_YET = "Not Committed Yet";
  private static final Subject<Boolean> isActive = BehaviorSubject.createDefault(true);
  private static final int FREE_SPACE = 6; // have to be modulo 2
  private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
  private final JTextComponent target;
  private final CompositeDisposable disposables = new CompositeDisposable();
  private BufferedImage blameImage;
  private Color foregroundColor;
  private Color backgroundColor;
  private Font nbFont;

  Annotator(Observable<Optional<IRepository>> pRepository, DataObject pDataObject, JTextComponent pTarget)
  {
    target = pTarget;
    setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setLocation(0, 0);
    File file = new File(pDataObject.getPrimaryFile().toURI());
    Observable<String> actualText = DocumentObservable.create(target.getDocument());

    // Observable to check the File changes between the latest version of the file on disk and the actual content of the file
    Observable<List<IFileChangeChunk>> chunkObservable = Observable
        .combineLatest(pRepository, actualText, (pRepoOpt, pText) -> {
          if (pRepoOpt.isPresent())
          {
            IRepository repo = pRepoOpt.get();
            EChangeType changeType = repo.getStatusOfSingleFile(file).getChangeType();
            // No changes if added or new, because the file can not be diffed -> not in index
            if (changeType == EChangeType.NEW || changeType == EChangeType.ADD)
              return List.of();
            return repo.diffOffline(pText, file);
          }
          return List.of();
        });

    Observable<Boolean> triggerUpdate = Observable.combineLatest(Observable.create(new _ScrollObservable(_getJScollPane(target))), isActive,
                                                                 (pRect, pIsActive) -> pIsActive && blameImage == null ||
                                                                     !pIsActive && blameImage != null).filter(pVal -> pVal);
    Observable<Optional<IBlame>> blameObservable = pRepository
        .switchMap(optRepo -> optRepo.map(pRepo -> pRepo.getBlame(file)).orElse(Observable.just(Optional.<IBlame>empty())));
    disposables.add(Observable.combineLatest(blameObservable, chunkObservable, isActive, triggerUpdate,
                                             (pBlame, pChunks, pIsActive, pTriggerUpdate) -> {
                                               if (!pBlame.isPresent() || target.getHeight() <= 0)
                                                 return Optional.<BufferedImage>empty();
                                               else
                                                 return _calculateImage(pIsActive, pBlame.get(), pChunks);

                                             }).subscribe(pBufferedImageOpt -> blameImage = pBufferedImageOpt.orElse(null)));
  }

  public static void setActive(boolean pIsActive)
  {
    isActive.onNext(pIsActive);
  }

  /**
   * @param pIsActive if the annotations should be shown
   * @param pBlame    IBlame object containing the information about the authors of lines
   * @param pChunks   List of IFileChangeChunks describing the changes of the last saved version of the file to the version in the editor
   * @return Optional of a BufferedImage, empty if not active, BufferedImage with the names of the authors and commit dates otherwise
   */
  private Optional<BufferedImage> _calculateImage(boolean pIsActive, IBlame pBlame, List<IFileChangeChunk> pChunks)
  {
    if (pIsActive)
    {
      List<String> stringList = _calculateStringList(pBlame, pChunks);
      View view = target.getUI().getRootView(target);
      BufferedImage rawBlameImage = new BufferedImage(getPreferredSize().width, target.getHeight(), BufferedImage.TYPE_INT_ARGB);
      _updateColorsAndFont(target);
      setBackground(backgroundColor);
      Graphics imageGraphics = rawBlameImage.getGraphics();
      imageGraphics.setFont(nbFont);
      imageGraphics.setColor(foregroundColor);
      int fontHeight = imageGraphics.getFontMetrics().getAscent();
      ((Graphics2D) imageGraphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      imageGraphics.setColor(foregroundColor);
      for (int i = 0; i < stringList.size(); i++)
      {
        _LineHolder holder = _calculateRec(stringList.get(i), i, view);
        if (holder != null)
        {
          Rectangle viewRect = holder.viewRect;
          int x = viewRect.x;
          int y = viewRect.y + fontHeight + Math.round((viewRect.height - getFontMetrics(nbFont).getHeight()) / 2f);
          imageGraphics.drawString(holder.text, x, y);
        }
      }
      return Optional.of(rawBlameImage);
    }
    return Optional.empty();
  }

  /**
   * Sets the Font and background/foregroundColor in the same way that the Netbeans GlyphGutter does
   * Copied from the GlyphGutter
   *
   * @param pTextComponent JTextComponent of the editor that this annotator belongs to
   */
  private void _updateColorsAndFont(JTextComponent pTextComponent)
  {
    EditorUI eui = ((BaseTextUI) pTextComponent.getUI()).getEditorUI();
    if (eui == null)
      return;
    Coloring lineColoring = eui.getColoringMap().get(FontColorNames.LINE_NUMBER_COLORING);
    Coloring defaultColoring = eui.getDefaultColoring();

    // fix for issue #16940
    // the real cause of this problem is that closed document is not garbage collected,
    // because of *some* references (see #16072) and so any change in AnnotationTypes.PROP_*
    // properties is fired which must update this component although it is not visible anymore
    if (lineColoring == null)
      return;


    final Color backColor = lineColoring.getBackColor();
    // set to white by o.n.swing.plaf/src/org/netbeans/swing/plaf/aqua/AquaLFCustoms
    if (org.openide.util.Utilities.isMac())
    {
      backgroundColor = backColor;
    }
    else
    {
      backgroundColor = UIManager.getColor("NbEditorGlyphGutter.background"); //NOI18N
    }
    if (null == backgroundColor && backColor != null)
    {
      backgroundColor = backColor;
    }
    if (backgroundColor != null)
    {
      setBackground(backgroundColor);
    }
    if (lineColoring.getForeColor() != null)
      foregroundColor = lineColoring.getForeColor();
    else
      foregroundColor = defaultColoring.getForeColor();

    Font lineFont;
    if (lineColoring.getFont() != null)
    {
      Font f = lineColoring.getFont();
      lineFont = (f != null) ? f.deriveFont((float) f.getSize() - 1) : null;
    }
    else
    {
      lineFont = defaultColoring.getFont();
      lineFont = new Font("Monospaced", Font.PLAIN, lineFont.getSize() - 1); //NOI18N
    }
    nbFont = lineFont;
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
      String sourceAuthor = pBlame.getSourceAuthor(i);
      //if the pLine is not committed the author should be blank
      list.add(NOT_COMMITTED_YET.equals(sourceAuthor) ? "" : timeString + " - " + sourceAuthor);
    }

    /*
    get the longest name to set the width to the panel
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
      pStringList.remove(pChunk.getBStart());
  }

  /**
   * Calculates one single annotation, for one line. It checks the if the line intersects the viewPort (pScrollRectangle)
   * and returns a _LineHolder if that is the case, or null if not
   *
   * @param pSourceAuthor The author of the line of code
   * @param pLine         the line of the code
   * @return return a {@code _LineHolder} which combines the size and position of an annotation and the source author
   */
  @Nullable
  private _LineHolder _calculateRec(String pSourceAuthor, int pLine, View pView)
  {
    // get the offset to set the annotation at the right height and width
    Element lineElement = target.getDocument().getDefaultRootElement().getElement(pLine);
    int startOffset = lineElement.getStartOffset();
    int endOffset = lineElement.getEndOffset();


    if (pView != null)
    {
      Rectangle changeRectangle;
      try
      {
        changeRectangle = pView.modelToView(startOffset, Position.Bias.Forward, endOffset, Position.Bias.Backward, new Rectangle()).getBounds();
        changeRectangle.setSize(FREE_SPACE / 2, target.getFontMetrics(target.getFont()).getHeight());
      }
      catch (BadLocationException pE)
      {
        throw new RuntimeException("can't get the model of the rectangle", pE);
      }

      return new _LineHolder(changeRectangle, pSourceAuthor);
    }

    return null;
  }

  @Override
  protected void paintComponent(Graphics pG)
  {
    super.paintComponent(pG);
    pG.drawImage(blameImage, 0, 0, null);
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
   * The rectangles will only be rendered if the clipping is shown.
   */
  private class _ScrollObservable extends AbstractListenerObservable<AdjustmentListener, JScrollPane, Rectangle>
  {
    private final JScrollPane target;

    _ScrollObservable(JScrollPane pTarget)
    {
      super(pTarget);
      target = pTarget;
    }

    @NotNull
    @Override
    protected AdjustmentListener registerListener(@NotNull JScrollPane pTarget, @NotNull IFireable<Rectangle> pFireable)
    {
      AdjustmentListener adjustmentListener = e -> pFireable.fireValueChanged(pTarget.getViewport().getViewRect());
      target.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);
      return adjustmentListener;
    }

    @Override
    protected void removeListener(@NotNull JScrollPane pListenableValue, @NotNull AdjustmentListener pAdjustmentListener)
    {
      target.getVerticalScrollBar().removeAdjustmentListener(pAdjustmentListener);
    }
  }
}
