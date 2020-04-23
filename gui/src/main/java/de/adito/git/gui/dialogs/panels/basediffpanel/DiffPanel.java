package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.api.data.diff.IFileDiff;
import de.adito.git.gui.Constants;
import de.adito.git.gui.LeftSideVSBScrollPaneLayout;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.LineNumbersColorModel;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.DiffPaneWrapper;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.impl.data.diff.DeltaTextChangeEventImpl;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Optional;

/**
 * Class to handle the basic layout of the two panels that display the differences between two files
 *
 * @author m.kaspera 12.11.2018
 */
public class DiffPanel extends JPanel implements IDiscardable
{

  private final DiffPaneWrapper currentVersionDiffPane;
  private final DiffPaneWrapper oldVersionDiffPane;
  private IDiffPaneUtil.ScrollBarCoupling differentialScrollBarCoupling;

  /**
   * @param pIconLoader          IIconLoader for loading Icons
   * @param pFileDiffObs         Observable of the IFileDiff
   * @param pAcceptIcon          ImageIcon that is used for the "accept these changes" button
   * @param pEditorKitObservable EditorKitObservable that holds the editorKit for the current IFileDiff
   */
  public DiffPanel(IIconLoader pIconLoader, @NotNull Observable<Optional<IFileDiff>> pFileDiffObs,
                   @Nullable ImageIcon pAcceptIcon, Observable<Optional<EditorKit>> pEditorKitObservable)
  {
    Observable<IDeltaTextChangeEvent> changesEventObservable = pFileDiffObs.switchMap(pFileDiff -> pFileDiff
        .map(IFileDiff::getDiffTextChangeObservable)
        .orElse(Observable.just(new DeltaTextChangeEventImpl(0, 0, "", null))));
    LineNumbersColorModel[] lineNumbersColorModels = new LineNumbersColorModel[2];
    DiffPanelModel currentDiffPanelModel = new DiffPanelModel(changesEventObservable, EChangeSide.NEW);
    currentVersionDiffPane = new DiffPaneWrapper(currentDiffPanelModel, pEditorKitObservable);
    // current panel is to the right, so index 1
    LineNumbersColorModel rightLineColorModel = currentVersionDiffPane.getPane().addLineNumPanel(currentDiffPanelModel, BorderLayout.WEST, 1);
    lineNumbersColorModels[1] = rightLineColorModel;
    JScrollPane currentVersionScrollPane = currentVersionDiffPane.getScrollPane();
    currentVersionScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    DiffPanelModel oldDiffPanelModel = new DiffPanelModel(changesEventObservable, EChangeSide.OLD)
        .setDoOnAccept(pChangeDelta -> pFileDiffObs.blockingFirst().ifPresent(pFileDiff -> pFileDiff.revertDelta(pChangeDelta)));
    oldVersionDiffPane = new DiffPaneWrapper(oldDiffPanelModel, pEditorKitObservable);

   // Neccessary for the left ChoiceButtonPanel, but should not be added to the Layout
    LineNumbersColorModel temp = oldVersionDiffPane.getPane().createLineNumberColorModel(oldDiffPanelModel, -1);

    // old version is to the left, so index 0
    LineNumbersColorModel leftLineColorsModel = oldVersionDiffPane.getPane().createLineNumberColorModel(oldDiffPanelModel, 0);
    lineNumbersColorModels[0] = leftLineColorsModel;

    oldVersionDiffPane.getPane().addChoiceButtonPanel(oldDiffPanelModel, pAcceptIcon, null, new LineNumbersColorModel[]{temp, leftLineColorsModel},
                                                      BorderLayout.EAST);
    oldVersionDiffPane.getPane().addLineNumPanel(leftLineColorsModel, oldDiffPanelModel, BorderLayout.EAST);
    oldVersionDiffPane.getPane().addChoiceButtonPanel(oldDiffPanelModel, null, null, lineNumbersColorModels, BorderLayout.EAST);

    JScrollPane oldVersionScrollPane = oldVersionDiffPane.getScrollPane();
    oldVersionScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    oldVersionScrollPane.setLayout(new LeftSideVSBScrollPaneLayout());
    setLayout(new BorderLayout());
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, oldVersionDiffPane.getPane(), currentVersionDiffPane.getPane());
    mainSplitPane.setResizeWeight(0.5);
    setBorder(new JScrollPane().getBorder());
    add(mainSplitPane, BorderLayout.CENTER);
    add(_initToolBar(pIconLoader), BorderLayout.NORTH);
    // couple horizontal scrollbars
    IDiffPaneUtil.bridge(List.of(currentVersionScrollPane.getHorizontalScrollBar().getModel(), oldVersionScrollPane.getHorizontalScrollBar().getModel()));
    differentialScrollBarCoupling = IDiffPaneUtil.synchronize(oldVersionDiffPane, null, currentVersionDiffPane, null, pFileDiffObs);
  }

  @Override
  public void discard()
  {
    currentVersionDiffPane.discard();
    oldVersionDiffPane.discard();
    differentialScrollBarCoupling.discard();
  }

  private JToolBar _initToolBar(IIconLoader pIconLoader)
  {
    JButton nextButton = new JButton(pIconLoader.getIcon(Constants.NEXT_OCCURRENCE));
    nextButton.setFocusable(false);
    nextButton.addActionListener(e -> {
      if (currentVersionDiffPane.isEditorFocusOwner())
        currentVersionDiffPane.moveCaretToNextChunk(oldVersionDiffPane.getEditorPane());
      else
        oldVersionDiffPane.moveCaretToNextChunk(currentVersionDiffPane.getEditorPane());
    });
    JButton previousButton = new JButton(pIconLoader.getIcon(Constants.PREVIOUS_OCCURRENCE));
    previousButton.setFocusable(false);
    previousButton.addActionListener(e -> {
      if (currentVersionDiffPane.isEditorFocusOwner())
        currentVersionDiffPane.moveCaretToPreviousChunk(oldVersionDiffPane.getEditorPane());
      else
        oldVersionDiffPane.moveCaretToPreviousChunk(currentVersionDiffPane.getEditorPane());
    });
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(nextButton);
    toolBar.add(previousButton);
    return toolBar;
  }
}