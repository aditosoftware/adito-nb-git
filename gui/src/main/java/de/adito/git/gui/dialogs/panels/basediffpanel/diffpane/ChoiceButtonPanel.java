package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.ColorPicker;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import de.adito.git.gui.swing.LineNumber;
import de.adito.git.gui.swing.SwingUtil;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel that contains buttons for accepting and discarding changes of a text displayed in a JTextPane
 * Position
 *
 * @author m.kaspera 13.12.2018
 */
class ChoiceButtonPanel extends JPanel implements IDiscardable, LineNumberListener, ViewLineChangeMarkingsListener, IconInfoModelListener
{

  @NonNull
  private final JViewport viewport;
  @NonNull
  private final LineNumberModel lineNumberModel;
  @NonNull
  private final ViewLineChangeMarkingModel leftMarkingsModel;
  @NonNull
  private final ViewLineChangeMarkingModel rightMarkingsModel;
  @Nullable
  private final ImageIcon discardIcon;
  @Nullable
  private final ImageIcon acceptIcon;
  @NonNull
  private List<ChangedChunkConnection> changedChunkConnectionsToDraw = new ArrayList<>();
  @NonNull
  private final IconInfoModel iconInfoModel;

  /**
   * @param pModel              DiffPanelModel containing information about which parts of the FileChangeChunk should be utilized
   * @param pEditorPane         EditorPane that contains the text for which the buttons should be drawn
   * @param pViewport           JViewPort containing pEditorPane
   * @param pLineNumberModel    LineNumberModel that tracks the coordinates of the lineNumbers/y coordinates of the lines in the editor
   * @param pLeftMarkingsModel  ViewLineChangeMarkingModel that contains the coordinates of the colored areas that should be displayed on the left side of this panel
   * @param pRightMarkingsModel ViewLineChangeMarkingModel that contains the coordinates of the colored areas that should be displayed on the right side of this panel
   * @param pAcceptIcon         icon used for the accept action
   * @param pDiscardIcon        icon used for the discard option. Null if the panel should allow the accept action only
   * @param pOrientation        String with the orientation (as BorderLayout.EAST/WEST) of this panel, determines the order of accept/discardButtons
   */
  ChoiceButtonPanel(@NonNull DiffPanelModel pModel, @NonNull JEditorPane pEditorPane, @NonNull JViewport pViewport, @NonNull LineNumberModel pLineNumberModel,
                    @NonNull ViewLineChangeMarkingModel pLeftMarkingsModel, @NonNull ViewLineChangeMarkingModel pRightMarkingsModel,
                    @Nullable ImageIcon pAcceptIcon, @Nullable ImageIcon pDiscardIcon, @NonNull String pOrientation)
  {
    viewport = pViewport;
    lineNumberModel = pLineNumberModel;
    leftMarkingsModel = pLeftMarkingsModel;
    rightMarkingsModel = pRightMarkingsModel;
    discardIcon = pDiscardIcon;
    acceptIcon = pAcceptIcon;
    int acceptIconWidth = pAcceptIcon != null ? pAcceptIcon.getIconWidth() : 16;
    setPreferredSize(new Dimension(_getSuggestedWidth(), 1));
    setBackground(ColorPicker.DIFF_BACKGROUND);
    iconInfoModel = new IconInfoModel(lineNumberModel, pModel.getChangeSide(), pEditorPane, pAcceptIcon, pDiscardIcon, pOrientation);
    if (pAcceptIcon != null || pDiscardIcon != null)
    {
      addMouseListener(new IconPressMouseAdapter(acceptIconWidth, pModel.getDoOnAccept(), pModel.getDoOnDiscard(),
                                                 iconInfoModel, pViewport::getViewRect, BorderLayout.WEST.equals(pOrientation)));
    }
    iconInfoModel.addListener(this);
    lineNumberModel.addListener(this);
    leftMarkingsModel.addListener(this);
    rightMarkingsModel.addListener(this);
  }

  private void _recalcAndDraw(JViewport pViewport)
  {
    changedChunkConnectionsToDraw = _calculateChunkConnectionsToDraw(pViewport.getViewRect(), leftMarkingsModel.getLineNumberColors(), rightMarkingsModel.getLineNumberColors());
    SwingUtil.invokeInEDT(() -> {
      revalidate();
      repaint();
    });
  }

  @Override
  public void discard()
  {
    lineNumberModel.removeListener(this);
    rightMarkingsModel.removeListener(this);
    leftMarkingsModel.removeListener(this);
    iconInfoModel.removeListener(this);
  }

  @Override
  protected void paintComponent(Graphics pGraphics)
  {
    super.paintComponent(pGraphics);
    _paintIcons(pGraphics, changedChunkConnectionsToDraw);
  }

  /**
   * calculates the connections that have to be drawn between the associated chunks of two panes
   * Has to calculate all the connections first since each time one of the scrollPanes moves, the areas of the connections changes, the areas are
   * seldom static. Filters the areas on if they  have parts visible and only returns those that do
   *
   * @param pDisplayArea Coordinates of the viewPort window, in view coordinates
   * @return List of ChangeChunkConnections that have to be drawn
   */
  @NonNull
  private List<ChangedChunkConnection> _calculateChunkConnectionsToDraw(Rectangle pDisplayArea, List<LineNumberColor> pLeftLineNumberColors,
                                                                        List<LineNumberColor> pRightLineNumberColors)
  {
    List<ChangedChunkConnection> changeChunksConnections = new ArrayList<>();
    if (pLeftLineNumberColors.size() == pRightLineNumberColors.size())
    {
      Rectangle viewPortArea = new Rectangle(0, 0, getWidth(), pDisplayArea.height);
      for (int index = 0; index < pLeftLineNumberColors.size(); index++)
      {
        if (pLeftLineNumberColors.get(index).getColoredArea().intersects(viewPortArea) || pRightLineNumberColors.get(index).getColoredArea().intersects(viewPortArea)
            || _isDifferentSigns(pLeftLineNumberColors.get(index).getColoredArea().y, pRightLineNumberColors.get(index).getColoredArea().y))
        {
          int[] xCoordinates = {0, 0, getPreferredSize().width, getPreferredSize().width};
          int[] yCoordinates = {pLeftLineNumberColors.get(index).getColoredArea().y + pLeftLineNumberColors.get(index).getColoredArea().height,
                                pLeftLineNumberColors.get(index).getColoredArea().y,
                                pRightLineNumberColors.get(index).getColoredArea().y,
                                pRightLineNumberColors.get(index).getColoredArea().y + pRightLineNumberColors.get(index).getColoredArea().height};
          Polygon connectionArea = new Polygon(xCoordinates, yCoordinates, 4);
          changeChunksConnections.add(new ChangedChunkConnection(connectionArea, pLeftLineNumberColors.get(index).getColor()));
        }
      }
    }
    return changeChunksConnections;
  }

  private boolean _isDifferentSigns(int pNum, int pSecondNum)
  {
    return (pNum > 0 && pSecondNum < 0) || (pNum < 0 && pSecondNum > 0);
  }

  /**
   * Extracted to separate method to take advantage of the fact that java gives us a reference to the list we pass. If the list itself is never
   * changed and instead the list is only exchanged with another one (like above) this should mean that there are no concurrentModificationExceptions
   * and we do not need any locks or copied immutable lists
   *
   * @param pGraphics Graphics object to paint with
   * @param pChangedChunkConnectionsToDraw List of ChangedChunkConnections that represent the connections of the colored areas of the left- and right LineChangeMarkingModels
   */
  private void _paintIcons(@NonNull Graphics pGraphics, @NonNull List<ChangedChunkConnection> pChangedChunkConnectionsToDraw)
  {
    // first draw the colored connections
    ((Graphics2D) pGraphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    for (ChangedChunkConnection chunkConnection : pChangedChunkConnectionsToDraw)
    {
      pGraphics.setColor(chunkConnection.getColor());
      pGraphics.fillPolygon(chunkConnection.getShape());
    }

    // then draw the icons so the icons are visible above the colored areas
    Rectangle viewRect = viewport.getViewRect();
    List<IconInfo> iconInfosToDraw = iconInfoModel.getIconInfosToDraw(viewRect.y, viewRect.y + viewRect.height);
    for (IconInfo iconInfo : iconInfosToDraw)
    {
      pGraphics.drawImage(iconInfo.getImageIcon().getImage(), iconInfo.getIconCoordinates().x, iconInfo.getIconCoordinates().y - viewRect.y, null);
    }
  }

  private int _getSuggestedWidth()
  {
    int discardIconWidth = discardIcon != null ? discardIcon.getIconWidth() : 0;
    return acceptIcon == null ? 16 : acceptIcon.getIconWidth() + discardIconWidth;
  }

  @Override
  public void lineNumbersChanged(@NonNull IDeltaTextChangeEvent pTextChangeEvent, @NonNull LineNumber[] pLineNumbers)
  {
    // do nothing, either the iconInfos or the viewLineChangeMarkins should also change -> will get notification about that -> don't do the same work twice
  }

  @Override
  public void viewLineChangeMarkingChanged(@NonNull List<LineNumberColor> pAdaptedLineNumberColorList)
  {
    _recalcAndDraw(viewport);
  }

  @Override
  public void iconInfosChanged()
  {
    _recalcAndDraw(viewport);
  }
}
