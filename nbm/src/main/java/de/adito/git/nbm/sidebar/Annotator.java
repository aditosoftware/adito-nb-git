package de.adito.git.nbm.sidebar;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBlame;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IChangeDelta;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.rxjava.ScrollBarExtentObservable;
import de.adito.git.gui.swing.LineNumber;
import de.adito.git.gui.swing.TextPaneUtil;
import de.adito.git.impl.observables.PropertyChangeObservable;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.actions.ShowAnnotationNBAction;
import de.adito.git.nbm.util.DocumentObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.openide.loaders.DataObject;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


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
  private static final int DEBOUNCE_DURATION = 100;
  private final Logger logger = Logger.getLogger(Annotator.class.getName());
  private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
  private final JTextComponent target;
  private final CompositeDisposable disposables = new CompositeDisposable();
  private BufferedImage blameImage;
  private Color foregroundColor;
  private Color backgroundColor;
  private Font nbFont;
  private boolean isActiveFlag = false;
  private MouseListener popupMouseListener;
  private IBlame blame = null;

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
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(new ShowAnnotationNBAction(target));
    PropertyChangeListener listener = evt -> _ancestorChanged(pRepository, pTarget, file, popupMenu, evt);
    addPropertyChangeListener(listener);
  }

  private void _ancestorChanged(Observable<Optional<IRepository>> pRepository, JTextComponent pTarget, File pFile, JPopupMenu pPopupMenu, PropertyChangeEvent evt)
  {
    if ("ancestor".equals(evt.getPropertyName()))
    {
      if (evt.getNewValue() == null)
      {
        discard();
        if (popupMouseListener != null)
        {
          removeMouseListener(popupMouseListener);
        }
      }
      else if (evt.getOldValue() == null)
      {
        _buildObservableChain(pRepository, pTarget, pFile);
        if (popupMouseListener == null)
        {
          popupMouseListener = new PopupMouseListener(pPopupMenu);
          addMouseListener(popupMouseListener);
        }
      }
    }
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
      disposables.clear();
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
    // Observable that fires each time the user changes the text in the textComponent
    Observable<String> textObservable = DocumentObservable.create(target.getDocument())
        .debounce(DEBOUNCE_DURATION, TimeUnit.MILLISECONDS);
    // Observable that fires if the size of the horizontal scrollbar changes
    Observable<Integer> scrollBarExtentObs = Observable.create(new ScrollBarExtentObservable(_getJScrollPane(target)))
        .debounce(DEBOUNCE_DURATION, TimeUnit.MILLISECONDS);
    // Observable that observes the Active flag for the Annotator that is stored in the client settings of the target textComponent
    Observable<Optional<Boolean>> isActive = BehaviorSubject.create(new PropertyChangeObservable<Boolean>(pTarget, IGitConstants.ANNOTATOR_ACTIVF_FLAG))
        .startWith(Optional.of(Boolean.FALSE));

    // Observable to check the File changes between the latest version of the file on disk and the actual content of the file
    Observable<List<IChangeDelta>> deltaObservable = Observable
        .combineLatest(pRepository, textObservable, isActive, (pRepoOpt, pText, pIsActive) -> {
          // only run the diff if the repo is present and the active flag is given
          if (pRepoOpt.isPresent() && pIsActive.orElse(false))
          {
            IRepository repo = pRepoOpt.get();
            // No check for new or deleted file (not in index in that case) since we just catch all Exceptions and if anything doesnt work we just do not show anything
            try
            {
              // TODO: try and get rid of the blockingfirst, possibly do switchMap
              return repo.diffOffline(pText, pFile).getChangeDeltas();
            }
            catch (Exception pE)
            {
              return List.of();
            }
          }
          return List.of();
        });

    // Observable that singals that the bufferedImage has to be updated. This is the case if the active flag is set and the maximum size of the scrollBar changes
    Observable<Boolean> triggerUpdate = Observable
        .combineLatest(scrollBarExtentObs, isActive, (pRect, pIsActive) -> {
          isActiveFlag = pIsActive.orElse(false);
          return pIsActive.orElse(false);
        })
        // no distinctUntilChanged here since we want the Observable to fire each time the scrollBar extent changes (provided isActive is true, hence the filter)
        .filter(pVal -> pVal);

    // combine Observables to create an Observable of the BufferedImage, then subscribe and draw it each time it changes
    disposables.add(Observable.combineLatest(pRepository, deltaObservable, isActive, triggerUpdate, (pRepoOpt, pDeltas, pIsActive, pTriggerUpdate)
        -> pRepoOpt.flatMap(pRepo -> _getBlameImage(pFile, pRepo, pDeltas, pIsActive.orElse(false)))).doOnError(pThrowable -> _showImage(pTarget, null))
                        .subscribe(pBufferedImageOpt -> _showImage(pTarget, pBufferedImageOpt.orElse(null))));
  }

  /**
   * Draws the image if the image if not null
   *
   * @param pTarget        textComponent for whose opened file the Annotator should do the git blame
   * @param pBufferedImage bufferedImage with the git blame lines
   */
  private void _showImage(JTextComponent pTarget, @Nullable BufferedImage pBufferedImage)
  {
    if (pBufferedImage != null)
    {
      blameImage = pBufferedImage;
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
  }

  /**
   * @param pFile     file of the editor, get the git blame for this
   * @param pRepo     repository used to retrieve the git blame
   * @param pDeltas   List with changed and unchanged chunks of the contents of the file
   * @param pIsActive whether or not the Annotator is active
   * @return Optional with a BufferedImage of the git blame, or an empty Optional if the target height is 0 or the Annotator is inactive
   */
  private Optional<BufferedImage> _getBlameImage(File pFile, IRepository pRepo, List<IChangeDelta> pDeltas, Boolean pIsActive)
  {
    // no need to calculate the Image if the Annotator is inactive or the height is 0 (aka Annotator is not shown)
    if (target.getHeight() <= 0 || !pIsActive)
      return Optional.empty();
    else
    {
      if (blame == null)
        pRepo.getBlame(pFile).ifPresent(pIBlame -> blame = pIBlame);
      return blame == null ? Optional.empty() : Optional.of(_calculateImage(blame, pDeltas));
    }
  }

  /**
   * @param pBlame  IBlame object containing the information about the authors of lines
   * @param pDeltas List of IFileChangeChunks describing the changes of the last saved version of the file to the version in the editor
   * @return Optional of a BufferedImage, empty if not active, BufferedImage with the names of the authors and commit dates otherwise
   */
  private BufferedImage _calculateImage(IBlame pBlame, List<IChangeDelta> pDeltas)
  {
    List<String> annotatedLines = _calculateStringList(pBlame, pDeltas);
    View view = target.getUI().getRootView(target);
    BufferedImage rawBlameImage = new BufferedImage(getPreferredSize().width, target.getHeight(), BufferedImage.TYPE_INT_ARGB);
    _updateColorsAndFont(target);
    setBackground(backgroundColor);
    try
    {
      LineNumber[] lineNumberPositions = TextPaneUtil.calculateLineYPositions(target, view);
      _drawImage(rawBlameImage.getGraphics(), annotatedLines, lineNumberPositions);
    }
    catch (BadLocationException pE)
    {
      logger.log(Level.SEVERE, pE, () -> "Git: Accessed bad index while trying to determine the positions of a line in the text editor");
    }
    catch (Error pE)
    {
      if (pE.getMessage().contains("Interrupted mutex acquiring"))
        logger.log(Level.WARNING, pE, () -> "Git: error while trying to access the document to determine the location of a line, skipping evaluation of that line");
      else throw new Error(pE);
    }
    return rawBlameImage;
  }

  /**
   * @param pImageGraphics       Graphics object of i.e. a bufferedImage
   * @param pLines               List of Strings with the information about what to write for each line. Sorted by order in which they appear
   * @param pLineNumberPositions View of the JTextComponent, used to get the y Coordinates of the lines
   */
  private void _drawImage(Graphics pImageGraphics, List<String> pLines, LineNumber[] pLineNumberPositions)
  {
    pImageGraphics.setFont(nbFont);
    pImageGraphics.setColor(foregroundColor);
    int fontHeight = pImageGraphics.getFontMetrics().getAscent();
    ((Graphics2D) pImageGraphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    pImageGraphics.setColor(foregroundColor);
    for (int lineIndex = 0; lineIndex < pLines.size(); lineIndex++)
    {
      _drawString(pImageGraphics, pLines, pLineNumberPositions, fontHeight, lineIndex);
    }
  }

  private void _drawString(Graphics pImageGraphics, List<String> pLines, LineNumber[] pLineNumberPositions, int pFontHeight, int pFinalLineIndex)
  {
    if (pFinalLineIndex < pLineNumberPositions.length)
    {
      Rectangle changeRectangle = new Rectangle();
      changeRectangle.setSize(FREE_SPACE / 2, target.getFontMetrics(target.getFont()).getHeight());
      int x = pLineNumberPositions[pFinalLineIndex].getXCoordinate();
      int y = pLineNumberPositions[pFinalLineIndex].getYCoordinate() + pFontHeight +
          Math.round((target.getFontMetrics(target.getFont()).getHeight() - getFontMetrics(nbFont).getHeight()) / 2f);
      pImageGraphics.drawString(pLines.get(pFinalLineIndex), x, y);
    }
  }

  /**
   * Calculate the list of Strings to print in each line as annotations
   *
   * @param pBlame  the "blame" annotation of the file
   * @param pDeltas the deltas of the file
   * @return Returns a list which contains the annotations (with the new lines which has no annotations)
   */
  private List<String> _calculateStringList(IBlame pBlame, List<IChangeDelta> pDeltas)
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

    //get all deltas to check the type
    for (IChangeDelta delta : pDeltas)
    {
      if (delta.getChangeType() == EChangeType.ADD)
      {
        _addLines(delta, list);
      }
      if (delta.getChangeType() == EChangeType.DELETE)
      {
        _deleteLines(delta, list);
      }
      if (delta.getChangeType() == EChangeType.MODIFY)
      {
        _deleteLines(delta, list);
        _addLines(delta, list);
      }
    }
    return list;
  }

  /**
   * Add a new Line to the annotator
   *
   * @param pDelta      the delta to check the lines to add
   * @param pStringList the string list to change the content
   */
  private void _addLines(IChangeDelta pDelta, List<String> pStringList)
  {
    int linesToAdd = pDelta.getEndLine(EChangeSide.NEW) - pDelta.getStartLine(EChangeSide.NEW);
    for (int i = 0; i < linesToAdd; i++)
      pStringList.add(pDelta.getStartLine(EChangeSide.NEW), "");
  }

  /**
   * Delete one line of the annotator
   *
   * @param pDelta      the delta to check the lines to delete
   * @param pStringList the string list to change the content
   */
  private void _deleteLines(IChangeDelta pDelta, List<String> pStringList)
  {
    int linesToDelete = pDelta.getEndLine(EChangeSide.OLD) - pDelta.getStartLine(EChangeSide.OLD);
    for (int i = 0; i < linesToDelete; i++)
      pStringList.remove(pDelta.getStartLine(EChangeSide.NEW));
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
}
