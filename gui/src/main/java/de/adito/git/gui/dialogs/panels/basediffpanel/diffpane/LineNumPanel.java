package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.ColorPicker;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.gui.swing.LineNumber;
import de.adito.git.gui.swing.SwingUtil;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * Panel that contains the line numbers of a given JTextPane. Arranges the numbers such that they fit the lines in the TextPane even if the font is
 * changed or there are virtual line breaks
 *
 * @author m.kaspera 13.12.2018
 */
class LineNumPanel extends JPanel implements IDiscardable, LineNumberColorsListener, LineNumberListener, ChangeListener
{

  /**
   * This variable sets how many pixels we use as safety margin for drawing lines around the visible rectangle
   */
  private static final int SAFETY_MARGIN_Y = 32;

  private final CompositeDisposable disposable = new CompositeDisposable();
  private final Insets panelInsets = new Insets(0, 0, 0, 0);
  private final Insets editorInsets;
  private final LineNumberModel lineNumberModel;
  private final LineChangeMarkingModel lineChangeMarkingModel;
  private final JViewport viewport;
  private final JEditorPane editorPane;
  private int lineNumFacadeWidth;


  /**
   * @param pEditorPane             JEditorPane that displays the text from the IFileChangeChunks in the pModel
   * @param pLineNumberModel        Model that keeps track of the y coordinates for the lineNumbers that this Panel should draw
   * @param pLineChangeMarkingModel Model that keeps track of the y coordinates for the colored areas (representation of the changes) that this Panel should draw
   * @param pViewport               ViewPort that is around the editorPane. Required so we know when the view coordinates of the viewPort change, and we can update this panel
   */
  LineNumPanel(@NotNull JEditorPane pEditorPane, @NotNull LineNumberModel pLineNumberModel, @NotNull LineChangeMarkingModel pLineChangeMarkingModel, @NotNull JViewport pViewport)
  {
    editorPane = pEditorPane;
    editorInsets = pEditorPane.getInsets();

    lineNumberModel = pLineNumberModel;
    lineNumberModel.addListener(this);

    lineChangeMarkingModel = pLineChangeMarkingModel;
    lineChangeMarkingModel.addListener(this);


    viewport = pViewport;
    viewport.addChangeListener(this);

    lineNumFacadeWidth = _calculateLineWidth();
    setPreferredSize(new Dimension(lineNumFacadeWidth + panelInsets.left + panelInsets.right, 1));
    setBorder(new EmptyBorder(panelInsets));
    setBackground(ColorPicker.DIFF_BACKGROUND);
  }

  /**
   * recalculate the width of this panel and redraw it
   */
  private void recalcAndRedraw()
  {
    SwingUtil.invokeInEDT(() -> {

                            lineNumFacadeWidth = _calculateLineWidth();
                            setPreferredSize(new Dimension(lineNumFacadeWidth + panelInsets.left + panelInsets.right, 1));
                            revalidate();
                            repaint();
                          }
    );
  }

  @Override
  protected void paintComponent(Graphics pGraphics)
  {
    super.paintComponent(pGraphics);
    Rectangle visibleRect = editorPane.getVisibleRect();

    // get the LineNumberColor we have to draw. We use the visible rect and some safety margin (32 pixels in this case) to identify only the visible LineNumberColor
    Collection<LineNumberColor> staticLineNumberColors = lineChangeMarkingModel.getLineNumberColorsToDraw(Math.max(0, visibleRect.y - SAFETY_MARGIN_Y), visibleRect.y + visibleRect.height + SAFETY_MARGIN_Y);
    for (LineNumberColor lineNumberColor : staticLineNumberColors)
    {
      pGraphics.setColor(lineNumberColor.getColor());
      pGraphics.fillRect(lineNumberColor.getColoredArea().x, lineNumberColor.getColoredArea().y - visibleRect.y,
                         getWidth(), lineNumberColor.getColoredArea().height);
    }

    // get the lineNumbers we have to draw. We use the visible rect and some safety margin (32 pixels in this case) to identify only the visible lineNumbers
    // the lineNumbers are drawn after the LineNumberColors because they should be visible above those colored fields
    Collection<LineNumber> linesToDraw = lineNumberModel.getLineNumbersToDraw(Math.max(0, visibleRect.y - SAFETY_MARGIN_Y), visibleRect.y + visibleRect.height + SAFETY_MARGIN_Y);
    ((Graphics2D) pGraphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    pGraphics.setColor(ColorPicker.DIFF_LINE_NUM);
    for (LineNumber lineNumber : linesToDraw)
    {
      pGraphics.drawString(lineNumber.getNumber(), lineNumber.getXCoordinate() + 2,
                           lineNumber.getYCoordinate() + pGraphics.getFontMetrics().getAscent() - editorInsets.top + 2 - visibleRect.y);
    }
  }

  @Override
  public void discard()
  {
    disposable.dispose();
    lineNumberModel.removeListener(this);
    lineChangeMarkingModel.removeListener(this);
    viewport.removeChangeListener(this);
  }

  @Override
  public void lineNumberColorsChanged(@NotNull List<LineNumberColor> pNewValue)
  {
    recalcAndRedraw();
  }

  @Override
  public void lineNumbersChanged(@NotNull IDeltaTextChangeEvent pTextChangeEvent, @NotNull LineNumber[] pLineNumbers)
  {
    recalcAndRedraw();
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

  @Override
  public void stateChanged(ChangeEvent e)
  {
    recalcAndRedraw();
  }
}
