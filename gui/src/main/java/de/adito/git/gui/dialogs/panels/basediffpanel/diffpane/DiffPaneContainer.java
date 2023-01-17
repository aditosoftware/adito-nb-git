package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

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
   * @param pTextChangeEventObservable Observable that fires a new DeltaTextChangeEvent if the text on any side of the Diff changes
   * @return LineNumberModel that keeps track of the y coordinates for each line in the editor of this diffPane
   */
  @NotNull
  public LineNumberModel createLineNumberModel(@NotNull Observable<IDeltaTextChangeEvent> pTextChangeEventObservable)
  {
    return diffPane.createLineNumberModel(pTextChangeEventObservable);
  }

  /**
   * /**
   * Creates a new LineNumPanel and adds it to the layout
   *
   * @param pLineNumberModel        LineNumberModel that keeps track of the y coordinates of the lines
   * @param pLineChangeMarkingModel LineChangeMarkingsModel that keeps track of the y coordinates of the change markings
   * @param pLineOrientation        String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   *                                Defaults to BorderLayout.WEST if another String is passed
   */
  public void addLineNumPanel(@NotNull LineNumberModel pLineNumberModel, @NotNull LineChangeMarkingModel pLineChangeMarkingModel, @NotNull String pLineOrientation)
  {
    diffPane.addLineNumPanel(pLineNumberModel, pLineChangeMarkingModel, pLineOrientation);
  }

  /**
   * Creates a new ChoiceButtonPanel and adds it to the layout
   *
   * @param pModel              DiffPanelModel with the Observable list of fileChangeChunks
   * @param pLineNumberModel    LineNumberModel that keeps track of the y coordinates of the lines
   * @param pLeftMarkingsModel  ViewLineChangeMarkingsModel that keeps track of the position of the changeMarkings relative to the viewport
   * @param pRightMarkingsModel ViewLineChangeMarkingsModel that keeps track of the position of the changeMarkings relative to the viewport
   * @param pAcceptIcon         ImageIcon for the accept button
   * @param pDiscardIcon        ImageIcon for the discard button
   * @param pOrientation        String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   */
  public void addChoiceButtonPanel(@NotNull DiffPanelModel pModel, @NotNull LineNumberModel pLineNumberModel, @NotNull ViewLineChangeMarkingModel pLeftMarkingsModel,
                                   @NotNull ViewLineChangeMarkingModel pRightMarkingsModel, @Nullable ImageIcon pAcceptIcon, @Nullable ImageIcon pDiscardIcon,
                                   @NotNull String pOrientation)
  {
    diffPane.addChoiceButtonPanel(pModel, pLineNumberModel, pLeftMarkingsModel, pRightMarkingsModel, pAcceptIcon, pDiscardIcon, pOrientation);
  }

  @Override
  public void discard()
  {
    diffPane.discard();
  }
}
