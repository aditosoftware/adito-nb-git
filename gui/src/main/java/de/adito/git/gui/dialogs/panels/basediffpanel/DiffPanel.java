package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.Constants;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.LineNumbersColorModel;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.DiffPaneWrapper;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.impl.data.FileChangesEventImpl;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.util.Collections;
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

  /**
   * @param pIconLoader          IIconLoader for loading Icons
   * @param pFileDiffObs         Observable of the IFileDiff
   * @param pAcceptIcon          ImageIcon that is used for the "accept these changes" button
   * @param pEditorKitObservable EditorKitObservable that holds the editorKit for the current IFileDiff
   */
  public DiffPanel(IIconLoader pIconLoader, @NotNull Observable<Optional<IFileDiff>> pFileDiffObs,
                   @Nullable ImageIcon pAcceptIcon, Observable<EditorKit> pEditorKitObservable)
  {
    Observable<IFileChangesEvent> changesEventObservable = pFileDiffObs.switchMap(pFileDiff -> pFileDiff
        .map(pDiff -> pDiff.getFileChanges().getChangeChunks())
        .orElse(Observable.just(new FileChangesEventImpl(true, Collections.emptyList()))));
    LineNumbersColorModel[] lineNumbersColorModels = new LineNumbersColorModel[2];
    DiffPanelModel currentDiffPanelModel = new DiffPanelModel(changesEventObservable,
                                                              IFileChangeChunk::getBLines, IFileChangeChunk::getBParityLines,
                                                              IFileChangeChunk::getBStart, IFileChangeChunk::getBEnd);
    currentVersionDiffPane = new DiffPaneWrapper(currentDiffPanelModel, pEditorKitObservable);
    // current panel is to the right, so index 1
    LineNumbersColorModel rightLineColorModel = currentVersionDiffPane.getPane().addLineNumPanel(currentDiffPanelModel, BorderLayout.WEST, 1);
    lineNumbersColorModels[1] = rightLineColorModel;
    JScrollPane currentVersionScrollPane = currentVersionDiffPane.getScrollPane();
    currentVersionScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    DiffPanelModel oldDiffPanelModel = new DiffPanelModel(changesEventObservable,
                                                          IFileChangeChunk::getALines, IFileChangeChunk::getAParityLines,
                                                          IFileChangeChunk::getAStart, IFileChangeChunk::getAEnd)
        .setDoOnAccept(pChangeChunk -> pFileDiffObs.blockingFirst().ifPresent(pFileDiff -> pFileDiff.getFileChanges().resetChanges(pChangeChunk)));
    oldVersionDiffPane = new DiffPaneWrapper(oldDiffPanelModel, pEditorKitObservable);
    // old version is to the left, so index 0
    LineNumbersColorModel leftLineColorsModel = oldVersionDiffPane.getPane().addLineNumPanel(oldDiffPanelModel, BorderLayout.EAST, 0);
    lineNumbersColorModels[0] = leftLineColorsModel;
    oldVersionDiffPane.getPane().addChoiceButtonPanel(oldDiffPanelModel, pAcceptIcon, null, lineNumbersColorModels,
                                                      BorderLayout.EAST);
    JScrollPane oldVersionScrollPane = oldVersionDiffPane.getScrollPane();
    oldVersionScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    oldVersionScrollPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    _bridge(oldVersionScrollPane.getVerticalScrollBar().getModel(), currentVersionScrollPane.getVerticalScrollBar().getModel());
    setLayout(new BorderLayout());
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, oldVersionDiffPane.getPane(), currentVersionDiffPane.getPane());
    mainSplitPane.setResizeWeight(0.5);
    setBorder(new JScrollPane().getBorder());
    add(mainSplitPane, BorderLayout.CENTER);
    add(_initToolBar(pIconLoader), BorderLayout.NORTH);
  }

  @Override
  public void discard()
  {
    currentVersionDiffPane.discard();
    oldVersionDiffPane.discard();
  }

  private JToolBar _initToolBar(IIconLoader pIconLoader)
  {
    JButton nextButton = new JButton(pIconLoader.getIcon(Constants.NEXT_OCCURRENCE));
    nextButton.setFocusable(false);
    nextButton.addActionListener(e -> {
      if (currentVersionDiffPane.isEditorFocusOwner())
        currentVersionDiffPane.moveCaretToNextChunk();
      else
        oldVersionDiffPane.moveCaretToNextChunk();
    });
    JButton previousButton = new JButton(pIconLoader.getIcon(Constants.PREVIOUS_OCCURRENCE));
    previousButton.setFocusable(false);
    previousButton.addActionListener(e -> {
      if (currentVersionDiffPane.isEditorFocusOwner())
        currentVersionDiffPane.moveCaretToPreviousChunk();
      else
        oldVersionDiffPane.moveCaretToPreviousChunk();
    });
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(nextButton);
    toolBar.add(previousButton);
    return toolBar;
  }

  private static void _bridge(BoundedRangeModel pModel1, BoundedRangeModel pModel2)
  {
    pModel1.addChangeListener(pE -> pModel2.setValue(pModel1.getValue()));
    pModel2.addChangeListener(pE -> pModel1.setValue(pModel2.getValue()));
  }

}