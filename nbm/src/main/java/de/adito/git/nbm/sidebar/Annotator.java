package de.adito.git.nbm.sidebar;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.util.DocumentObservable;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;
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
import java.beans.PropertyChangeListener;
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
  private static final int FREE_SPACE = 6; // have to be modulo 2
  private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
  private final JTextComponent target;
  private final CompositeDisposable disposables = new CompositeDisposable();
  private BufferedImage blameImage;
  private Color foregroundColor;
  private Color backgroundColor;
  private Font nbFont;
  private boolean isActiveFlag = false;

  /**
   * @param pRepository Observable of the current Repository that also contains the File currently open in the editor
   * @param pDataObject DataObject from Netbeans
   * @param pTarget     JTextComponent of the Netbeans editor
   */
  Annotator(Observable<Optional<IRepository>> pRepository, DataObject pDataObject, JTextComponent pTarget)
  {
    target = pTarget;
    setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setLocation(0, 0);
    foregroundColor = pTarget.getForeground();
    backgroundColor = pTarget.getBackground();
    nbFont = pTarget.getFont();
    File file = new File(pDataObject.getPrimaryFile().toURI());
    _buildObservableChain(pRepository, pTarget, file);
  }

  /**
   * Method overridden to provide 0 max size if the annotator is inactive
   *
   * @return dimension of 0 width and height if inactive, the value from super otherwise
   */
  @Override
  public Dimension getMaximumSize()
  {
    if (isActiveFlag)
      return super.getMaximumSize();
    else
      return new Dimension(0, 0);
  }

  @Override
  public void discard()
  {
    if (!disposables.isDisposed())
      disposables.dispose();
  }

  @Override
  protected void paintComponent(Graphics pG)
  {
    super.paintComponent(pG);
    pG.drawImage(blameImage, 0, 0, null);
  }


  /**
   * @param pRepository Observable of the current Repository that also contains the File currently open in the editor
   * @param pTarget     JTextComponent of the Netbeans editor
   * @param pFile       File that is currently opened in the editor
   */
  private void _buildObservableChain(Observable<Optional<IRepository>> pRepository, JTextComponent pTarget, File pFile)
  {
    Observable<String> actualText = DocumentObservable.create(target.getDocument());

    // Observable to check the File changes between the latest version of the file on disk and the actual content of the file
    Observable<List<IFileChangeChunk>> chunkObservable = Observable
        .combineLatest(pRepository, actualText, (pRepoOpt, pText) -> {
          if (pRepoOpt.isPresent())
          {
            IRepository repo = pRepoOpt.get();
            // No check for new or deleted file (not in index in that case) since we just catch all Exceptions and if anything doesnt work we just do not show anything
            try
            {
              return repo.diffOffline(pText, pFile);
            }
            catch (Exception pE)
            {
              return List.of();
            }
          }
          return List.of();
        });

    Observable<Boolean> isActive = BehaviorSubject.create(new _ActiveObservable(pTarget)).startWith(false);
    Observable<Boolean> triggerUpdate = Observable.combineLatest(Observable.create(new _ScrollObservable(_getJScrollPane(target))), isActive,
                                                                 (pRect, pIsActive) -> {
                                                                   isActiveFlag = pIsActive;
                                                                   return pIsActive;
                                                                 }).filter(pVal -> pVal);
    Observable<Optional<IBlame>> blameObservable = pRepository
        .switchMap(optRepo -> optRepo.map(pRepo -> pRepo.getBlame(pFile)).orElse(Observable.just(Optional.<IBlame>empty())));
    disposables.add(Observable.combineLatest(blameObservable, chunkObservable, isActive, triggerUpdate,
                                             (pBlame, pChunks, pIsActive, pTriggerUpdate) -> {
                                               if (!pBlame.isPresent() || target.getHeight() <= 0)
                                                 return Optional.<BufferedImage>empty();
                                               else
                                                 return _calculateImage(pIsActive, pBlame.get(), pChunks);
                                             })
                        .subscribe(pBufferedImageOpt -> {
                          if (pBufferedImageOpt.isPresent())
                          {
                            blameImage = pBufferedImageOpt.get();
                            setSize(new Dimension(100, 100));
                          }
                          else
                          {
                            blameImage = null;
                            setSize(new Dimension(0, 0));
                            setPreferredSize(new Dimension(0, 0));
                          }
                          SwingUtilities.invokeLater(() -> {
                            pTarget.revalidate();
                            repaint();
                          });
                        }));
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
      List<String> annotatedLines = _calculateStringList(pBlame, pChunks);
      View view = target.getUI().getRootView(target);
      BufferedImage rawBlameImage = new BufferedImage(getPreferredSize().width, target.getHeight(), BufferedImage.TYPE_INT_ARGB);
      _updateColorsAndFont(target);
      setBackground(backgroundColor);
      _drawImage(rawBlameImage.getGraphics(), annotatedLines, view);
      return Optional.of(rawBlameImage);
    }
    return Optional.empty();
  }

  /**
   * @param pImageGraphics Graphics object of i.e. a bufferedImage
   * @param pLines         List of Strings with the information about what to write for each line. Sorted by order in which they appear
   * @param pView          View of the JTextComponent, used to get the y Coordinates of the lines
   */
  private void _drawImage(Graphics pImageGraphics, List<String> pLines, View pView)
  {
    pImageGraphics.setFont(nbFont);
    pImageGraphics.setColor(foregroundColor);
    int fontHeight = pImageGraphics.getFontMetrics().getAscent();
    ((Graphics2D) pImageGraphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    pImageGraphics.setColor(foregroundColor);
    if (pView != null)
    {
      for (int lineIndex = 0; lineIndex < pLines.size(); lineIndex++)
      {
        // get the offset to set the annotation at the right height and width
        Element lineElement = target.getDocument().getDefaultRootElement().getElement(lineIndex);
        int startOffset = lineElement.getStartOffset();
        int endOffset;
        if (lineIndex + 1 == pLines.size())
        {
          endOffset = lineElement.getEndOffset();
        }
        else
        {
          endOffset = target.getDocument().getDefaultRootElement().getElement(lineIndex + 1).getStartOffset();
        }

        Rectangle changeRectangle;
        try
        {
          changeRectangle = pView.modelToView(startOffset, Position.Bias.Forward, endOffset, Position.Bias.Backward, new Rectangle()).getBounds();
          changeRectangle.setSize(FREE_SPACE / 2, target.getFontMetrics(target.getFont()).getHeight());
          int x = changeRectangle.x;
          int y = changeRectangle.y + fontHeight + Math.round((changeRectangle.height - getFontMetrics(nbFont).getHeight()) / 2f);
          pImageGraphics.drawString(pLines.get(lineIndex), x, y);
        }
        catch (BadLocationException pE)
        {
          throw new RuntimeException(pE);
        }
      }
    }
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
    int linesToAdd = pChunk.getEnd(EChangeSide.NEW) - pChunk.getStart(EChangeSide.NEW);
    for (int i = 0; i < linesToAdd; i++)
      pStringList.add(pChunk.getStart(EChangeSide.NEW), "");
  }

  /**
   * Delete one line of the annotator
   *
   * @param pChunk      the chunk to check the lines to delete
   * @param pStringList the string list to change the content
   */
  private void _deleteLines(IFileChangeChunk pChunk, List<String> pStringList)
  {
    int linesToDelete = pChunk.getEnd(EChangeSide.OLD) - pChunk.getStart(EChangeSide.OLD);
    for (int i = 0; i < linesToDelete; i++)
      pStringList.remove(pChunk.getStart(EChangeSide.NEW));
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

  /**
   * Sets the Font and background/foregroundColor in the same way that the Netbeans GlyphGutter does
   * Copied from the GlyphGutter
   *
   * @param pTextComponent JTextComponent of the editor that this annotator belongs to
   */
  private void _updateColorsAndFont(JTextComponent pTextComponent)
  {
    if (pTextComponent.getUI() instanceof BaseTextUI)
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
  }


  /*
   * An observable to check the values on the ScrollPane.
   * this is important for the rectangles inside the editor bar.
   * The rectangles will only be rendered if the clipping is shown.
   */
  private class _ScrollObservable extends AbstractListenerObservable<AdjustmentListener, JScrollPane, Integer>
  {
    private final JScrollPane target;
    private int lastMaxValue = 0;

    _ScrollObservable(JScrollPane pTarget)
    {
      super(pTarget);
      target = pTarget;
    }

    @NotNull
    @Override
    protected AdjustmentListener registerListener(@NotNull JScrollPane pTarget, @NotNull IFireable<Integer> pFireable)
    {
      AdjustmentListener adjustmentListener = e -> {
        if (lastMaxValue != pTarget.getVerticalScrollBar().getMaximum())
        {
          lastMaxValue = pTarget.getVerticalScrollBar().getMaximum();
          pFireable.fireValueChanged(pTarget.getVerticalScrollBar().getMaximum());
        }
      };
      target.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);
      return adjustmentListener;
    }

    @Override
    protected void removeListener(@NotNull JScrollPane pListenableValue, @NotNull AdjustmentListener pAdjustmentListener)
    {
      target.getVerticalScrollBar().removeAdjustmentListener(pAdjustmentListener);
    }
  }

  /**
   * Observable that keeps track of the clientProperty with key IGitConstants
   */
  private class _ActiveObservable extends AbstractListenerObservable<PropertyChangeListener, JTextComponent, Boolean>
  {
    private final JTextComponent target;

    _ActiveObservable(JTextComponent pTarget)
    {
      super(pTarget);
      target = pTarget;
    }

    @NotNull
    @Override
    protected PropertyChangeListener registerListener(@NotNull JTextComponent pTarget, @NotNull IFireable<Boolean> pFireable)
    {
      PropertyChangeListener propertyChangeListener = e -> {
        if (e.getPropertyName().equals(IGitConstants.ANNOTATOR_ACTIVF_FLAG) && e.getNewValue() instanceof Boolean)
        {
          pFireable.fireValueChanged(e.getNewValue() == null || (Boolean) e.getNewValue());
        }
      };
      target.addPropertyChangeListener(propertyChangeListener);
      return propertyChangeListener;
    }

    @Override
    protected void removeListener(@NotNull JTextComponent pListenableValue, @NotNull PropertyChangeListener pPropertyChangeListener)
    {
      target.removePropertyChangeListener(pPropertyChangeListener);
    }
  }
}
