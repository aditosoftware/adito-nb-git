package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.ColorPicker;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Panel that contains the line numbers of a given JTextPane. Arranges the numbers such that they fit the lines in the TextPane even if the font is
 * changed or there are virtual line breaks
 *
 * @author m.kaspera 13.12.2018
 */
class LineNumPanel extends JPanel implements IDiscardable, ILineNumberColorsListener
{

  private final DiffPanelModel model;
  private final Disposable areaDisposable;
  private final Disposable sizeDisposable;
  private final Insets panelInsets = new Insets(0, 3, 0, 3);
  private final Insets editorInsets;
  private Rectangle cachedViewRectangle = new Rectangle();
  // lists with Objects that contain information about what to draw. Never modify these lists by themselves, only re-assign them
  private List<LineNumberColor> lineNumberColors = new ArrayList<>(); // all LineNumberColors for the file in the editor
  private BufferedImage lineNumImage = null;
  private final LineNumbersColorModel lineNumbersColorModel;
  private final BehaviorSubject<Object> lineNumChangedObs = BehaviorSubject.create();
  private int lineNumFacadeWidth;


  /**
   * @param pModel           DiffPanelModel containing information about which parts of the FileChangeChunk should be utilized
   * @param pEditorPane      JEditorPane that displays the text from the IFileChangeChunks in the pModel
   * @param pDisplayedArea   Observable of the Rectangle of the viewPort. Changes each time the viewPort is moved or resized
   * @param pViewPortSizeObs Observable of the Dimension of the viewPort. Changes each time the viewPort has its size changed, and only then
   */
  LineNumPanel(@NotNull DiffPanelModel pModel, JEditorPane pEditorPane, @NotNull Observable<Rectangle> pDisplayedArea,
               Observable<Dimension> pViewPortSizeObs, @NotNull LineNumbersColorModel pLineNumbersColorModel)
  {
    model = pModel;
    lineNumbersColorModel = pLineNumbersColorModel;
    lineNumbersColorModel.addLazyListener(this);
    editorInsets = pEditorPane.getInsets();
    lineNumFacadeWidth = _calculateLineWidth(pModel.getFileChangesObservable().blockingFirst());
    setPreferredSize(new Dimension(lineNumFacadeWidth + panelInsets.left + panelInsets.right, 1));
    setBorder(new EmptyBorder(panelInsets));
    setBackground(ColorPicker.DIFF_BACKGROUND);
    sizeDisposable = Observable.combineLatest(
        pModel.getFileChangesObservable(), pViewPortSizeObs, lineNumChangedObs, ((pFileChangesEvent, pDimension, pObj) -> pFileChangesEvent))
        .subscribe(
            pFileChangeEvent -> SwingUtilities.invokeLater(() -> {
              lineNumFacadeWidth = _calculateLineWidth(pFileChangeEvent);
              setPreferredSize(new Dimension(lineNumFacadeWidth + panelInsets.left + panelInsets.right, 1));
              lineNumImage = _calculateLineNumImage(pEditorPane, pFileChangeEvent, lineNumberColors);
              repaint();
            }));
    areaDisposable = Observable.combineLatest(
        pModel.getFileChangesObservable(), pDisplayedArea, FileChangesRectanglePair::new).throttleLatest(16, TimeUnit.MILLISECONDS, true)
        .subscribe(
            pPair -> SwingUtilities.invokeLater(() -> {
              if (lineNumImage == null)
              {
                lineNumImage = _calculateLineNumImage(pEditorPane, pPair.getFileChangesEvent(), lineNumberColors);
              }
              cachedViewRectangle = pPair.getRectangle();
              repaint();
            }));
  }

  @Override
  protected void paintComponent(Graphics pGraphics)
  {
    super.paintComponent(pGraphics);
    pGraphics.drawImage(lineNumImage, panelInsets.left, 0, lineNumFacadeWidth + panelInsets.left, cachedViewRectangle.height,
                        0, cachedViewRectangle.y, lineNumFacadeWidth, cachedViewRectangle.y + cachedViewRectangle.height, ColorPicker.DIFF_BACKGROUND,
                        null);
  }

  @Override
  public void discard()
  {
    areaDisposable.dispose();
    sizeDisposable.dispose();
    lineNumbersColorModel.removeListener(this);
    lineNumbersColorModel.discard();
  }

  @Override
  public void lineNumberColorsChanged(int pModelNumber, List<LineNumberColor> pNewValue)
  {
    lineNumberColors = pNewValue;
    lineNumChangedObs.onNext(pNewValue);
  }

  /**
   * @param pEditorPane       JEditorPane containing the text for which theses lines are
   * @param pFileChangesEvent IFileChangesEvent with the latest list of IFileChangeChunks
   * @param pLineNumColors    Areas affected by the ChangeChunks
   * @return BufferedImage that represents the content of this panel
   */
  @Nullable
  private BufferedImage _calculateLineNumImage(@NotNull JEditorPane pEditorPane, @NotNull IFileChangesEvent pFileChangesEvent,
                                               @NotNull List<LineNumberColor> pLineNumColors)
  {
    View view = pEditorPane.getUI().getRootView(pEditorPane);
    if (pEditorPane.getHeight() <= 0)
      return null;
    try
    {
      Set<LineNumber> lineNums = _calculateLineNumbers(pEditorPane, view, pEditorPane.getFontMetrics(pEditorPane.getFont()).getHeight(), 0,
                                                       Math.max(0, _getLastLineNum(pFileChangesEvent) - 1));
      BufferedImage image = new BufferedImage(lineNumFacadeWidth, pEditorPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics graphics = image.getGraphics();
      for (LineNumberColor lineNumberColor : pLineNumColors)
      {
        graphics.setColor(lineNumberColor.getColor());
        graphics.fillRect(lineNumberColor.getColoredArea().x, lineNumberColor.getColoredArea().y,
                          getWidth(), lineNumberColor.getColoredArea().height);
      }
      ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.setColor(ColorPicker.DIFF_LINE_NUM);
      for (LineNumber lineNumber : lineNums)
      {
        graphics.drawString(lineNumber.getNumber(), lineNumber.getXCoordinate(),
                            lineNumber.getYCoordinate() + graphics.getFontMetrics().getAscent() - editorInsets.top);
      }
      return image;
    }
    catch (BadLocationException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * calculates the y coordinates of pStartIndex and pEndIndex lines and then checks if the lines in between have height of pLineHeight each.
   * If that is not the case, split the interval in 2 and check again
   * Finally return a Set with the coordinates for each line in the passed interval
   *
   * @param pEditorPane JEditorPane containing the text for which theses lines are
   * @param pView       view mapping the model to viewSpace
   * @param pLineHeight height of a line in the current font
   * @param pStartIndex first line to check
   * @param pEndIndex   last line to check
   * @return Set of LineNumbers
   * @throws BadLocationException if one of the accessed lineNumbers is out of bounds
   */
  private Set<LineNumber> _calculateLineNumbers(@NotNull JEditorPane pEditorPane, View pView, int pLineHeight, int pStartIndex, int pEndIndex)
      throws BadLocationException
  {
    Set<LineNumber> lineNumbers = new HashSet<>();
    LineNumber startNumber = _calculateLineNumberPos(pEditorPane, pView, pStartIndex);
    LineNumber endNumber = _calculateLineNumberPos(pEditorPane, pView, pEndIndex);
    if (startNumber == null || endNumber == null)
      return lineNumbers;
    // we're down to two lines here, if the first one is the on taking up more space the for loop in the "else if" gives the space to the second line,
    // so seperate treatment here
    if (pEndIndex - pStartIndex <= 1)
    {
      lineNumbers.add(new LineNumber(pStartIndex + 1, startNumber.getYCoordinate(), startNumber.getXCoordinate()));
      lineNumbers.add(new LineNumber(pEndIndex + 1, endNumber.getYCoordinate(), endNumber.getXCoordinate()));
    }
    else if (endNumber.getYCoordinate() - startNumber.getYCoordinate() == (pEndIndex - pStartIndex) * pLineHeight)
    {
      for (int index = 0; index <= pEndIndex - pStartIndex; index++)
      {
        lineNumbers.add(new LineNumber(pStartIndex + index + 1, startNumber.getYCoordinate() + index * pLineHeight, startNumber.getXCoordinate()));
      }
    }
    else
    {
      lineNumbers.addAll(_calculateLineNumbers(pEditorPane, pView, pLineHeight, pStartIndex, (pStartIndex + pEndIndex) / 2));
      lineNumbers.addAll(_calculateLineNumbers(pEditorPane, pView, pLineHeight, (pStartIndex + pEndIndex) / 2, pEndIndex));
    }
    return lineNumbers;
  }

  /**
   * @param pEditorPane JEditorPane containing the text for which theses lines are
   * @param pView       view mapping the model to viewSpace
   * @param pLineIndex  index of the line for which to calculate the position
   * @return LineNumber with the number of the line and its position
   * @throws BadLocationException if the lineIndex is out of bounds
   */
  @Nullable
  private LineNumber _calculateLineNumberPos(@NotNull JEditorPane pEditorPane, @NotNull View pView, int pLineIndex) throws BadLocationException
  {
    if (pEditorPane.getDocument().getDefaultRootElement().getElementCount() < pLineIndex)
      return null;
    Element lineElement = pEditorPane.getDocument().getDefaultRootElement().getElement(pLineIndex);
    if (lineElement == null)
      throw new BadLocationException("Element in Document for line was null", pLineIndex);
    int startOffset = lineElement.getStartOffset();
    int yViewCoordinate = pView.modelToView(startOffset, Position.Bias.Forward, startOffset + 1, Position.Bias.Forward, new Rectangle())
        .getBounds().y;
    return new LineNumber(pLineIndex + 1, yViewCoordinate, 0);
  }

  /**
   * calculate the width this panel must have to display all the lineNumbers, based on the highest lineNumber and the used font
   *
   * @param pChangesEvent IFileChangesEvent with the latest list of IFileChangeChunks
   * @return the number of the last line
   */
  private int _calculateLineWidth(@NotNull IFileChangesEvent pChangesEvent)
  {
    return getFontMetrics(getFont()).stringWidth(String.valueOf(_getLastLineNum(pChangesEvent)));
  }

  private int _getLastLineNum(IFileChangesEvent pChangesEvent)
  {
    // do a default value that should kinda fit all
    if (pChangesEvent == null)
      return 150;
    List<IFileChangeChunk> changeChunks = pChangesEvent.getNewValue();
    if (changeChunks.isEmpty())
      return 0;
    return changeChunks.get(changeChunks.size() - 1).getEnd(model.getChangeSide());
  }
}
