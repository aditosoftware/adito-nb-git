package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.ColorPicker;
import de.adito.git.api.IDiscardable;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import de.adito.git.gui.swing.LineNumber;
import de.adito.git.gui.swing.SwingUtil;
import de.adito.git.gui.swing.TextPaneUtil;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.View;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Panel that contains the line numbers of a given JTextPane. Arranges the numbers such that they fit the lines in the TextPane even if the font is
 * changed or there are virtual line breaks
 *
 * @author m.kaspera 13.12.2018
 */
class LineNumPanel extends JPanel implements IDiscardable, ILineNumberColorsListener
{

  private final Disposable areaDisposable;
  private final Disposable sizeDisposable;
  private final Insets panelInsets = new Insets(0, 0, 0, 0);
  private final Insets editorInsets;
  private Rectangle cachedViewRectangle = new Rectangle();
  // lists with Objects that contain information about what to draw. Never modify these lists by themselves, only re-assign them
  private List<LineNumberColor> lineNumberColors = new ArrayList<>(); // all LineNumberColors for the file in the editor
  private BufferedImage lineNumImage = null;
  private final JEditorPane editorPane;
  private final JViewport viewport;
  private final LineNumbersColorModel lineNumbersColorModel;
  private int lineNumFacadeWidth;


  /**
   * @param pModel           DiffPanelModel containing information about which parts of the FileChangeChunk should be utilized
   * @param pEditorPane      JEditorPane that displays the text from the IFileChangeChunks in the pModel
   * @param pViewport        JViewPort containing pEditorPane
   * @param pViewPortSizeObs Observable of the Dimension of the viewPort. Changes each time the viewPort has its size changed, and only then
   */
  LineNumPanel(@NotNull DiffPanelModel pModel, JEditorPane pEditorPane, @NotNull JViewport pViewport,
               Observable<Dimension> pViewPortSizeObs, @NotNull LineNumbersColorModel pLineNumbersColorModel)
  {
    editorPane = pEditorPane;
    viewport = pViewport;
    lineNumbersColorModel = pLineNumbersColorModel;
    lineNumbersColorModel.addLazyListener(this);
    editorInsets = pEditorPane.getInsets();
    lineNumFacadeWidth = _calculateLineWidth();
    setPreferredSize(new Dimension(lineNumFacadeWidth + panelInsets.left + panelInsets.right, 1));
    setBorder(new EmptyBorder(panelInsets));
    setBackground(ColorPicker.DIFF_BACKGROUND);
    editorPane.addPropertyChangeListener("completion-visible", pEvent -> {
      SwingUtilities.invokeLater(() -> {
        lineNumImage = _calculateLineNumImage(pEditorPane, lineNumberColors);
        lineNumFacadeWidth = _calculateLineWidth();
        setPreferredSize(new Dimension(lineNumFacadeWidth + panelInsets.left + panelInsets.right, 1));
        revalidate();
        repaint();
        editorPane.getParent().getParent().getParent().getParent().getParent().getParent().revalidate();
        editorPane.getParent().getParent().getParent().getParent().getParent().getParent().repaint();
      });
    });
    sizeDisposable = Observable.combineLatest(
        pModel.getFileChangesObservable(), pViewPortSizeObs, ((pFileChangesEvent, pDimension) -> pFileChangesEvent))
        .throttleLatest(200, TimeUnit.MILLISECONDS, true)
        .subscribe(
            pFileChangeEvent -> SwingUtil.invokeASAP(() -> {
              lineNumFacadeWidth = _calculateLineWidth();
              setPreferredSize(new Dimension(lineNumFacadeWidth + panelInsets.left + panelInsets.right, 1));
              lineNumImage = _calculateLineNumImage(pEditorPane, lineNumberColors);
              repaint();
            }));
    areaDisposable = pModel.getFileChangesObservable().subscribe(pChangeEvent -> _recalcAndRedraw(pEditorPane, viewport));
    pViewport.addChangeListener(e -> _recalcAndRedraw(pEditorPane, viewport));
  }

  private void _recalcAndRedraw(JEditorPane pEditorPane, @NotNull JViewport pViewport)
  {
    SwingUtil.invokeASAP(() -> {
      if (lineNumImage == null)
      {
        lineNumImage = _calculateLineNumImage(pEditorPane, lineNumberColors);
      }
      cachedViewRectangle = pViewport.getViewRect();
      repaint();
    });
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
    _recalcAndRedraw(editorPane, viewport);
  }

  /**
   * @param pEditorPane    JEditorPane containing the text for which theses lines are
   * @param pLineNumColors Areas affected by the ChangeChunks
   * @return BufferedImage that represents the content of this panel
   */
  @Nullable
  private BufferedImage _calculateLineNumImage(@NotNull JEditorPane pEditorPane, @NotNull List<LineNumberColor> pLineNumColors)
  {
    View view = pEditorPane.getUI().getRootView(pEditorPane);
    if (pEditorPane.getHeight() <= 0)
      return null;
    try
    {
      LineNumber[] lineNums = TextPaneUtil.calculateLineYPositions(pEditorPane, view);
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
        graphics.drawString(lineNumber.getNumber(), lineNumber.getXCoordinate() + 2,
                            lineNumber.getYCoordinate() + graphics.getFontMetrics().getAscent() - editorInsets.top + 2);
      }
      return image;
    }
    catch (BadLocationException pE)
    {
      throw new RuntimeException(pE);
    }
  }


  /**
   * calculate the width this panel must have to display all the lineNumbers, based on the highest lineNumber and the used font
   *
   * @return the number of the last line
   */
  private int _calculateLineWidth()
  {
    return getFontMetrics(getFont()).stringWidth(String.valueOf(editorPane.getDocument().getDefaultRootElement().getElementCount() - 1)) + 7;
  }
}
