package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.ColorPicker;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.EChangeStatus;
import de.adito.git.api.data.diff.IChangeDelta;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import de.adito.git.gui.swing.SwingUtil;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.View;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel that contains buttons for accepting and discarding changes of a text displayed in a JTextPane
 * Position
 *
 * @author m.kaspera 13.12.2018
 */
class ChoiceButtonPanel extends JPanel implements IDiscardable, ILineNumberColorsListener
{

  private final DiffPanelModel model;
  private final JViewport viewport;
  private final ImageIcon discardIcon;
  private final ImageIcon acceptIcon;
  private final LineNumbersColorModel[] lineNumbersColorModels;
  private final int acceptChangeIconXVal;
  private final int discardChangeIconXVal;
  private final Disposable areaDisposable;
  private final Disposable sizeDisposable;
  private final Insets panelInsets = new Insets(1, 0, 1, 0);
  private BufferedImage bufferedIconImage;
  private List<IconInfo> iconInfoList = new ArrayList<>();
  private List<LineNumberColor> leftLineNumberColors = new ArrayList<>();
  private List<LineNumberColor> rightLineNumberColors = new ArrayList<>();
  private List<ChangedChunkConnection> changedChunkConnectionsToDraw = new ArrayList<>();

  /**
   * @param pModel              DiffPanelModel containing information about which parts of the FileChangeChunk should be utilized
   * @param pEditorPane         EditorPane that contains the text for which the buttons should be drawn
   * @param pViewport           JViewPort containing pEditorPane
   * @param pViewPortSizeObs    Observable that changes each time the size of the viewPort changes, and only then
   * @param pAcceptIcon         icon used for the accept action
   * @param pDiscardIcon        icon used for the discard option. Null if the panel should allow the accept action only
   * @param pLineNumColorModels Array of size 2 with LineNumbersColorModel, index 0 is the to the left of this ChoiceButtonPane, 1 to the right
   * @param pOrientation        String with the orientation (as BorderLayout.EAST/WEST) of this panel, determines the order of accept/discardButtons
   */
  ChoiceButtonPanel(@NotNull DiffPanelModel pModel, JEditorPane pEditorPane, JViewport pViewport,
                    Observable<Dimension> pViewPortSizeObs, @Nullable ImageIcon pAcceptIcon, @Nullable ImageIcon pDiscardIcon,
                    LineNumbersColorModel[] pLineNumColorModels, String pOrientation)
  {
    model = pModel;
    viewport = pViewport;
    discardIcon = pDiscardIcon;
    acceptIcon = pAcceptIcon;
    lineNumbersColorModels = pLineNumColorModels;
    int acceptIconWidth = pAcceptIcon != null ? pAcceptIcon.getIconWidth() : 16;
    setPreferredSize(new Dimension(_getSuggestedWidth(), 1));
    setBackground(ColorPicker.DIFF_BACKGROUND);
    acceptChangeIconXVal = BorderLayout.WEST.equals(pOrientation) || pDiscardIcon == null ? 0 : pDiscardIcon.getIconWidth();
    discardChangeIconXVal = BorderLayout.WEST.equals(pOrientation) ? acceptIconWidth : 2;
    addMouseListener(new IconPressMouseAdapter(acceptIconWidth, pModel.getDoOnAccept(), pModel.getDoOnDiscard(), () -> iconInfoList, pViewport::getViewRect,
                                               BorderLayout.WEST.equals(pOrientation)));
    pLineNumColorModels[0].addEagerListener(this);
    pLineNumColorModels[1].addEagerListener(this);
    sizeDisposable = Observable.combineLatest(
        pModel.getFileChangesObservable(), pViewPortSizeObs, ((pFileChangesEvent, pDimension) -> pFileChangesEvent))
        .subscribe(
            pChangeEvent -> SwingUtil.invokeASAP(() -> {
              _calculateButtonViewCoordinates(pEditorPane, pChangeEvent);
              repaint();
            }));
    areaDisposable = pModel.getFileChangesObservable().subscribe(
        pEvent -> _recalcAndDraw(pViewport));
    pViewport.addChangeListener(pEvent -> _recalcAndDraw(pViewport));
  }

  private void _recalcAndDraw(JViewport pViewport)
  {
    SwingUtil.invokeASAP(() -> {
      changedChunkConnectionsToDraw = _calculateChunkConnectionsToDraw(pViewport.getViewRect(), leftLineNumberColors, rightLineNumberColors);
      repaint();
    });
  }

  @Override
  public void discard()
  {
    lineNumbersColorModels[0].discard();
    lineNumbersColorModels[1].discard();
    areaDisposable.dispose();
    sizeDisposable.dispose();
  }

  @Override
  protected void paintComponent(Graphics pGraphics)
  {
    super.paintComponent(pGraphics);
    _paintIcons(pGraphics, changedChunkConnectionsToDraw, bufferedIconImage, viewport.getViewRect());
  }

  /**
   * calculates the positions of all Buttons
   *
   * @param pEditorPane       JEditorPane that contains the text of the IFileChangeChunks
   * @param pFileChangesEvent most recent IFileChangesEvent
   */
  private void _calculateButtonViewCoordinates(@NotNull JEditorPane pEditorPane, IDeltaTextChangeEvent pFileChangesEvent)
  {
    List<IconInfo> iconInfos = new ArrayList<>();
    if (_getSuggestedWidth() <= 0 || pEditorPane.getHeight() <= 0)
    {
      bufferedIconImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      return;
    }
    BufferedImage iconImage = new BufferedImage(_getSuggestedWidth(), pEditorPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
    try
    {
      View view = pEditorPane.getUI().getRootView(pEditorPane);
      List<IChangeDelta> changeDeltas = pFileChangesEvent.getFileDiff() == null ? List.of() : pFileChangesEvent.getFileDiff().getChangeDeltas();
      for (IChangeDelta fileChange : changeDeltas)
      {
        // Chunks with type SAME have no buttons since contents are equal
        if (fileChange.getChangeStatus() == EChangeStatus.PENDING && pEditorPane.getDocument().getLength() > 0)
        {
          int characterStartOffset = fileChange.getStartTextIndex(model.getChangeSide());
          int yViewCoordinate = view.modelToView(characterStartOffset, Position.Bias.Forward, characterStartOffset + 1,
                                                 Position.Bias.Forward, new Rectangle()).getBounds().y + 2;
          if (acceptIcon != null)
            iconInfos.add(new IconInfo(acceptIcon, yViewCoordinate, acceptChangeIconXVal, fileChange));
          // discardIcon == null -> only accept button should be used (case DiffPanel)
          if (discardIcon != null)
          {
            iconInfos.add(new IconInfo(discardIcon, yViewCoordinate, discardChangeIconXVal, fileChange));
          }
        }
      }
      iconInfoList = iconInfos;
      Graphics graphics = iconImage.getGraphics();
      for (IconInfo iconInfo : iconInfos)
      {
        iconInfo.getImageIcon().paintIcon(this, graphics, iconInfo.getIconCoordinates().x, iconInfo.getIconCoordinates().y);
      }
      bufferedIconImage = iconImage;
    }
    catch (BadLocationException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * calculates the connections that have to be drawn between the associated chunks of two panes
   * Has to calculate all the connections first since each time one of the scrollPanes moves, the areas of the connections changes, the areas are
   * seldom static. Filters the areas on if they  have parts visible and only returns those that do
   *
   * @param pDisplayArea Coordinates of the viewPort window, in view coordinates
   * @return List of ChangeChunkConnections that have to be drawn
   */
  @NotNull
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
   */
  private void _paintIcons(Graphics pGraphics, List<ChangedChunkConnection> pChangedChunkConnectionsToDraw, BufferedImage pBufferedImage, Rectangle pViewArea)
  {
    ((Graphics2D) pGraphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    for (ChangedChunkConnection chunkConnection : pChangedChunkConnectionsToDraw)
    {
      pGraphics.setColor(chunkConnection.getColor());
      pGraphics.fillPolygon(chunkConnection.getShape());
    }
    pGraphics.drawImage(pBufferedImage, panelInsets.left, 0, _getSuggestedWidth() + panelInsets.left, pViewArea.height,
                        panelInsets.left, pViewArea.y, _getSuggestedWidth() + panelInsets.left, pViewArea.y + pViewArea.height, null);
  }

  @Override
  public void lineNumberColorsChanged(int pModelNumber, List<LineNumberColor> pNewValue)
  {
    if (pModelNumber == 0)
    {
      leftLineNumberColors = pNewValue;
    }
    else
    {
      rightLineNumberColors = pNewValue;
    }
    changedChunkConnectionsToDraw = _calculateChunkConnectionsToDraw(viewport.getViewRect(), leftLineNumberColors, rightLineNumberColors);
    repaint();
  }

  private int _getSuggestedWidth()
  {
    int discardIconWidth = discardIcon != null ? discardIcon.getIconWidth() : 0;
    return acceptIcon == null ? 16 : acceptIcon.getIconWidth() + discardIconWidth;
  }
}
