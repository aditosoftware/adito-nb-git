package de.adito.git.gui.dialogs.panels.BaseDiffPanel;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.Constants;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.DiffPane.LineNumbersColorModel;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.DiffPaneWrapper;
import de.adito.git.impl.data.FileChangesEventImpl;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

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
   * @param pFileDiffObs Observable of the IFileDiff
   * @param pAcceptIcon  ImageIcon that is used for the "accept these changes" button
   */
  public DiffPanel(@NotNull Observable<Optional<IFileDiff>> pFileDiffObs, @NotNull ImageIcon pAcceptIcon, Observable<EditorKit> pEditorKitObservable)
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
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, oldVersionDiffPane.getPane(), currentVersionDiffPane.getPane());
    mainSplitPane.setResizeWeight(0.5);
    setBorder(new JScrollPane().getBorder());
    add(mainSplitPane, BorderLayout.CENTER);
  }

  @Override
  public void discard()
  {
    currentVersionDiffPane.discard();
    oldVersionDiffPane.discard();
  }

  private static void _bridge(BoundedRangeModel pModel1, BoundedRangeModel pModel2)
  {
    pModel1.addChangeListener(pE -> pModel2.setValue(pModel1.getValue()));
    pModel2.addChangeListener(pE -> pModel1.setValue(pModel2.getValue()));
  }

}