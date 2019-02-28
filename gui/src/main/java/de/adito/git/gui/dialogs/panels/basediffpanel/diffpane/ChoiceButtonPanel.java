package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.ColorPicker;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
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
  private final ImageIcon discardIcon;
  private final ImageIcon acceptIcon;
  private final LineNumbersColorModel[] lineNumbersColorModels;
  private final String orientation;
  private final int acceptChangeIconXVal;
  private final int discardChangeIconXVal;
  private final Disposable disposable;
  private final Insets panelInsets = new Insets(1, 0, 1, 0);
  private Rectangle cachedViewRectangle = new Rectangle();
  private List<IconInfo> iconInfoList = new ArrayList<>();
  private List<IconInfo> iconInfosToDraw = new ArrayList<>();
  private List<LineNumberColor> leftLineNumberColors = new ArrayList<>();
  private List<LineNumberColor> rightLineNumberColors = new ArrayList<>();
  private List<ChangedChunkConnection> changedChunkConnectionsToDraw = new ArrayList<>();

  /**
   * @param pModel              DiffPanelModel that contains functions that retrieve information, such as start/end line, of an IFileChangeChunk
   * @param pEditorPane         EditorPane that contains the text for which the buttons should be drawn
   * @param pDisplayedArea      Observable with the Rectangle that defines the viewPort on the EditorPane
   * @param pAcceptIcon         icon used for the accept action
   * @param pDiscardIcon        icon used for the discard option. Null if the panel should allow the accept action only
   * @param pLineNumColorModels Array of size 2 with LineNumbersColorModel, index 0 is the to the left of this ChoiceButtonPane, 1 to the right
   * @param pOrientation        String with the orientation (as BorderLayout.EAST/WEST) of this panel, determines the order of accept/discardButtons
   */
  ChoiceButtonPanel(@NotNull DiffPanelModel pModel, JEditorPane pEditorPane, Observable<Rectangle> pDisplayedArea,
                    @Nullable ImageIcon pAcceptIcon, @Nullable ImageIcon pDiscardIcon, LineNumbersColorModel[] pLineNumColorModels,
                    String pOrientation)
  {
    model = pModel;
    discardIcon = pDiscardIcon;
    acceptIcon = pAcceptIcon;
    lineNumbersColorModels = pLineNumColorModels;
    orientation = pOrientation;
    int acceptIconWidth = pAcceptIcon != null ? pAcceptIcon.getIconWidth() : 16;
    setPreferredSize(new Dimension(acceptIconWidth + (pDiscardIcon != null ? pDiscardIcon.getIconWidth() : 0), 1));
    setBackground(ColorPicker.DIFF_BACKGROUND);
    acceptChangeIconXVal = BorderLayout.WEST.equals(pOrientation) || pDiscardIcon == null ? 0 : pDiscardIcon.getIconWidth();
    discardChangeIconXVal = BorderLayout.WEST.equals(pOrientation) ? acceptIconWidth : 0;
    addMouseListener(new IconPressMouseAdapter(acceptIconWidth, pModel.getDoOnAccept(), pModel.getDoOnDiscard(), () -> iconInfosToDraw,
                                               BorderLayout.WEST.equals(pOrientation)));
    pLineNumColorModels[0].addListener(this);
    pLineNumColorModels[1].addListener(this);
    disposable = Observable.combineLatest(
        pModel.getFileChangesObservable(), pDisplayedArea, FileChangesRectanglePair::new)
        .subscribe(
            pPair -> SwingUtilities.invokeLater(() -> {
              _calculateButtonViewCoordinates(pEditorPane, pPair.getFileChangesEvent(), pPair.getRectangle());
              changedChunkConnectionsToDraw = _calculateChunkConnectionsToDraw(pPair.getRectangle(), leftLineNumberColors, rightLineNumberColors);
              repaint();
            }));
  }

  @Override
  public void discard()
  {
    lineNumbersColorModels[0].discard();
    lineNumbersColorModels[1].discard();
    disposable.dispose();
  }

  @Override
  protected void paintComponent(Graphics pGraphics)
  {
    super.paintComponent(pGraphics);
    _paintIcons(pGraphics, changedChunkConnectionsToDraw, iconInfosToDraw);
  }

  /**
   * @param pEditorPane       JEditorPane that contains the text of the IFileChangeChunks
   * @param pFileChangesEvent most recent IFileChangesEvent
   * @param pDisplayedArea    Coordinates of the viewPort window, in view coordinates
   */
  private void _calculateButtonViewCoordinates(@NotNull JEditorPane pEditorPane, IFileChangesEvent pFileChangesEvent, Rectangle pDisplayedArea)
  {
    if ((cachedViewRectangle.height == 0 || cachedViewRectangle.width != pDisplayedArea.width || pDisplayedArea.equals(cachedViewRectangle))
        && acceptIcon != null)
    {
      List<IconInfo> iconInfos = new ArrayList<>();
      try
      {
        View view = pEditorPane.getUI().getRootView(pEditorPane);
        int lineNumber = 0;
        for (IFileChangeChunk fileChange : pFileChangesEvent.getNewValue())
        {
          // Chunks with type SAME have no buttons since contents are equal
          if (fileChange.getChangeType() != EChangeType.SAME)
          {
            Element lineElement = pEditorPane.getDocument().getDefaultRootElement().getElement(lineNumber);
            if (lineElement == null)
              throw new BadLocationException("lineElement for line was null", lineNumber);
            int characterStartOffset = lineElement.getStartOffset();
            int yViewCoordinate = view.modelToView(characterStartOffset, Position.Bias.Forward, characterStartOffset + 1,
                                                   Position.Bias.Forward, new Rectangle()).getBounds().y;
            iconInfos.add(new IconInfo(acceptIcon, yViewCoordinate, acceptChangeIconXVal, fileChange));
            // discardIcon == null -> only accept button should be used (case DiffPanel)
            if (discardIcon != null)
            {
              iconInfos.add(new IconInfo(discardIcon, yViewCoordinate, discardChangeIconXVal, fileChange));
            }
          }
          lineNumber += (fileChange.getEnd(model.getChangeSide()) - fileChange.getStart(model.getChangeSide()));
        }
        iconInfoList = iconInfos;
      }
      catch (BadLocationException pE)
      {
        throw new RuntimeException(pE);
      }
    }
    iconInfosToDraw = _calculateIconsToDraw(pEditorPane, pDisplayedArea, iconInfoList);
    cachedViewRectangle = pDisplayedArea;
  }

  /**
   * @param pEditorPane    JEditorPane that contains the text of the IFileChangeChunks
   * @param pDisplayedArea Coordinates of the viewPort window, in view coordinates
   * @param pIconInfos     List of all IconInfos of this Panel
   * @return List of IconInfos filtered by "do they have to be drawn"
   */
  private List<IconInfo> _calculateIconsToDraw(@NotNull JEditorPane pEditorPane, Rectangle pDisplayedArea, List<IconInfo> pIconInfos)
  {
    List<IconInfo> filteredIconInfos = new ArrayList<>();
    for (IconInfo iconInfo : pIconInfos)
    {
      if (pDisplayedArea.intersects(iconInfo.getIconCoordinates()))
      {
        filteredIconInfos.add(new IconInfo(iconInfo.getImageIcon(),
                                           iconInfo.getIconCoordinates().y - pDisplayedArea.y + pEditorPane.getInsets().top + panelInsets.top,
                                           iconInfo.getIconCoordinates().x, iconInfo.getFileChangeChunk()));
      }
    }
    return filteredIconInfos;
  }

  /**
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
        int[] xCoordinates = {0, 0, getPreferredSize().width, getPreferredSize().width};
        int[] yCoordinates = {pLeftLineNumberColors.get(index).getColoredArea().y + pLeftLineNumberColors.get(index).getColoredArea().height,
                              pLeftLineNumberColors.get(index).getColoredArea().y,
                              pRightLineNumberColors.get(index).getColoredArea().y,
                              pRightLineNumberColors.get(index).getColoredArea().y + pRightLineNumberColors.get(index).getColoredArea().height};
        Polygon connectionArea = new Polygon(xCoordinates, yCoordinates, 4);
        if (connectionArea.intersects(viewPortArea))
        {
          changeChunksConnections.add(new ChangedChunkConnection(connectionArea, pLeftLineNumberColors.get(index).getColor()));
        }
      }
    }
    return changeChunksConnections;
  }

  /**
   * Extracted to separate method to take advantage of the fact that java gives us a reference to the list we pass. If the list itself is never
   * changed and instead the list is only exchanged with another one (like above) this should mean that there are no concurrentModificationExceptions
   * and we do not need any locks or copied immutable lists
   *
   * @param pGraphics     Graphics object to paint with
   * @param pIconInfoList the list buttons/icons to be drawn
   */
  private void _paintIcons(Graphics pGraphics, List<ChangedChunkConnection> pChangedChunkConnectionsToDraw, List<IconInfo> pIconInfoList)
  {
    ((Graphics2D) pGraphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    for (ChangedChunkConnection chunkConnection : pChangedChunkConnectionsToDraw)
    {
      pGraphics.setColor(chunkConnection.getColor());
      pGraphics.fillPolygon(chunkConnection.getShape());
    }
    for (IconInfo iconInfo : pIconInfoList)
    {
      iconInfo.getImageIcon().paintIcon(this, pGraphics, iconInfo.getIconCoordinates().x, iconInfo.getIconCoordinates().y);
    }
  }

  @Override
  public void lineNumberColorsChanged(int pModelNumber, List<LineNumberColor> pNewValue)
  {
    if (pModelNumber == 0)
    {
      leftLineNumberColors = pNewValue;
      if (BorderLayout.WEST.equals(orientation))
      {
        changedChunkConnectionsToDraw = _calculateChunkConnectionsToDraw(cachedViewRectangle, leftLineNumberColors, rightLineNumberColors);
        repaint();
      }

    }
    else
    {
      rightLineNumberColors = pNewValue;
      if (!BorderLayout.WEST.equals(orientation))
      {
        repaint();
        changedChunkConnectionsToDraw = _calculateChunkConnectionsToDraw(cachedViewRectangle, leftLineNumberColors, rightLineNumberColors);
      }
    }
  }
}