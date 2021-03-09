package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.IDiscardable;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.util.Optional;

/**
 * Container around the DiffPane, used to decorate the DiffPane
 *
 * @author m.kaspera, 15.10.2020
 */
public class DiffPaneContainer extends JPanel implements IDiscardable
{

  private static final int MARGIN = 3;
  private final DiffPane diffPane;

  public DiffPaneContainer(@NotNull JEditorPane pEditorPane, @Nullable String pHeader, int pHeaderAlignment)
  {
    super(new BorderLayout());
    diffPane = new DiffPane(pEditorPane);
    add(diffPane, BorderLayout.CENTER);
    if (pHeader != null)
    {
      JLabel label = new JLabel(pHeader);
      label.setHorizontalAlignment(pHeaderAlignment);
      int leftMargin = pHeaderAlignment == SwingConstants.LEFT ? MARGIN : 0;
      int rightMargin = pHeaderAlignment == SwingConstants.RIGHT ? MARGIN : 0;
      label.setBorder(new EmptyBorder(MARGIN, leftMargin, MARGIN, rightMargin));
      add(label, BorderLayout.NORTH);
    }
  }

  public JScrollPane getScrollPane()
  {
    return diffPane.getScrollPane();
  }

  /**
   * @param pModel             DiffPanelModel with the Observable list of fileChangeChunks
   * @param pInitHeightCalcObs Observable that fires once when the heights are ready to be calculated
   * @param pLineOrientation   String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   *                           Defaults to BorderLayout.WEST if another String is passed
   * @param pModelNumber       fnumber of the LineNumbersColorModel used in the LineNumPanel, also returned by this method
   * @return LineNumbersColorModel describing the
   */
  public LineNumbersColorModel addLineNumPanel(DiffPanelModel pModel, Observable<Optional<Object>> pInitHeightCalcObs, String pLineOrientation, int pModelNumber)
  {
    return diffPane.addLineNumPanel(pModel, pInitHeightCalcObs, pLineOrientation, pModelNumber);
  }

  /**
   * Only creates the LineNumbersColorModel, it is not added to the Layout.
   *
   * @param pModel             DiffPanelModel with the Observable list of fileChangeChunks
   * @param pInitHeightCalcObs Observable that fires once when the heights are ready to be calculated
   * @param pModelNumber       number of the LineNumbersColorModel used in the LineNumPanel, also returned by this method
   * @return the new LineNumbersColorModel
   * @see #addLineNumPanel(DiffPanelModel, Observable, String, int)
   */
  public LineNumbersColorModel createLineNumberColorModel(DiffPanelModel pModel, Observable<Optional<Object>> pInitHeightCalcObs, int pModelNumber)
  {
    return diffPane.createLineNumberColorModel(pModel, pInitHeightCalcObs, pModelNumber);
  }

  /**
   * Adds the LineNumbersColorModel to the Layout. Therefore a LineNumPanel is created.
   *
   * @param pLineNumbersColorModel the LineNumbersColorModel, which should be added
   * @param pModel                 DiffPanelModel with the Observable list of fileChangeChunks
   * @param pLineOrientation       String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   *                               Defaults to BorderLayout.WEST if another String is passed
   */
  public void addLineNumPanel(LineNumbersColorModel pLineNumbersColorModel, DiffPanelModel pModel, String pLineOrientation)
  {
    diffPane.addLineNumPanel(pLineNumbersColorModel, pModel, pLineOrientation);
  }

  /**
   * @param pModel                  DiffPanelModel with the Observable list of fileChangeChunks
   * @param pAcceptIcon             ImageIcon for the accept button
   * @param pDiscardIcon            ImageIcon for the discard button
   * @param pLineNumbersColorModels Array of size 2 with LineNumPanels, index 0 is the LineNumPanel to the left of this ChoiceButtonPane,
   *                                1 to the right
   * @param pOrientation            String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   */
  public void addChoiceButtonPanel(@NotNull DiffPanelModel pModel, @Nullable ImageIcon pAcceptIcon, @Nullable ImageIcon pDiscardIcon,
                                   LineNumbersColorModel[] pLineNumbersColorModels, @NotNull String pOrientation)
  {
    diffPane.addChoiceButtonPanel(pModel, pAcceptIcon, pDiscardIcon, pLineNumbersColorModels, pOrientation);
  }

  @Override
  public void discard()
  {
    diffPane.discard();
  }
}
