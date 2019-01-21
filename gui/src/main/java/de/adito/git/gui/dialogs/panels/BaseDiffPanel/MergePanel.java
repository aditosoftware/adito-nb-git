package de.adito.git.gui.dialogs.panels.BaseDiffPanel;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.Constants;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.DiffPane.LineNumPanel;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.DiffPaneWrapper;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.ForkPointPaneWrapper;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;

/**
 * Class that handles the layout of the merge dialog and creates the elements in that form the dialog
 *
 * @author m.kaspera 12.11.2018
 */
public class MergePanel extends JPanel implements IDiscardable
{

  private final IMergeDiff mergeDiff;
  private final ImageIcon acceptYoursIcon;
  private final ImageIcon acceptTheirsIcon;
  private final ImageIcon discardIcon;
  private final Observable<EditorKit> editorKitObservable;
  private DiffPaneWrapper yoursPaneWrapper;
  private ForkPointPaneWrapper forkPointPaneWrapper;
  private DiffPaneWrapper theirsPaneWrapper;
  private LineNumPanel leftForkPointLineNumPanel;
  private LineNumPanel rightForkPointLineNumPanel;

  public MergePanel(IMergeDiff pMergeDiff, ImageIcon pAcceptYoursIcon, ImageIcon pAcceptTheirsIcon, ImageIcon pDiscardIcon,
                    IEditorKitProvider pEditorKitProvider)
  {
    mergeDiff = pMergeDiff;
    acceptYoursIcon = pAcceptYoursIcon;
    acceptTheirsIcon = pAcceptTheirsIcon;
    discardIcon = pDiscardIcon;
    editorKitObservable = Observable.just(pEditorKitProvider.getEditorKit(pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath()));
    _initForkPointPanel();
    _initYoursPanel();
    _initTheirsPanel();
    _initGui();
  }

  private void _initGui()
  {
    setLayout(new BorderLayout());
    JSplitPane forkMergeSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, forkPointPaneWrapper.getPane(), theirsPaneWrapper.getPane());
    JSplitPane threeWayPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, yoursPaneWrapper.getPane(), forkMergeSplit);
    // 0.5 so the initial split is equal. For perfect feel on resizing set to 1, this would make the right pane almost invisible at the start though
    forkMergeSplit.setResizeWeight(0.5);
    // 0.33 because the right side contains two sub-windows, the left only one
    threeWayPane.setResizeWeight(0.33);
    add(threeWayPane);
  }

  private void _initYoursPanel()
  {
    LineNumPanel[] lineNumPanels = new LineNumPanel[2];
    DiffPanelModel yoursModel = new DiffPanelModel(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges().getChangeChunks(),
                                                   IFileChangeChunk::getBLines, IFileChangeChunk::getBParityLines,
                                                   IFileChangeChunk::getBStart, IFileChangeChunk::getBEnd)
        .setDoOnAccept(pChangeChunk -> mergeDiff.acceptChunk(pChangeChunk, IMergeDiff.CONFLICT_SIDE.YOURS))
        .setDoOnDiscard(pChangeChunk -> mergeDiff.discardChange(pChangeChunk, IMergeDiff.CONFLICT_SIDE.YOURS));
    yoursPaneWrapper = new DiffPaneWrapper(yoursModel, editorKitObservable);
    yoursPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    yoursPaneWrapper.getScrollPane().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    // index 0 because the lineNumPanel is of the left-most panel, and thus to the left to the ChoiceButtonPanel
    lineNumPanels[0] = yoursPaneWrapper.getPane().addLineNumPanel(yoursModel, BorderLayout.EAST);
    lineNumPanels[1] = leftForkPointLineNumPanel;
    yoursPaneWrapper.getPane().addChoiceButtonPanel(yoursModel, acceptYoursIcon, discardIcon,
                                                    lineNumPanels,
                                                    BorderLayout.EAST);
  }

  private void _initTheirsPanel()
  {
    LineNumPanel[] lineNumPanels = new LineNumPanel[2];
    DiffPanelModel theirsModel = new DiffPanelModel(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS).getFileChanges().getChangeChunks(),
                                                    IFileChangeChunk::getBLines, IFileChangeChunk::getBParityLines,
                                                    IFileChangeChunk::getBStart, IFileChangeChunk::getBEnd)
        .setDoOnAccept(pChangeChunk -> mergeDiff.acceptChunk(pChangeChunk, IMergeDiff.CONFLICT_SIDE.THEIRS))
        .setDoOnDiscard(pChangeChunk -> mergeDiff.discardChange(pChangeChunk, IMergeDiff.CONFLICT_SIDE.THEIRS));
    theirsPaneWrapper = new DiffPaneWrapper(theirsModel, editorKitObservable);
    theirsPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    // index 1 because the lineNumPanel is of the right-most panel, and thus to the right to the ChoiceButtonPanel
    lineNumPanels[1] = theirsPaneWrapper.getPane().addLineNumPanel(theirsModel, BorderLayout.WEST);
    lineNumPanels[0] = rightForkPointLineNumPanel;
    theirsPaneWrapper.getPane().addChoiceButtonPanel(theirsModel, acceptTheirsIcon, discardIcon,
                                                     lineNumPanels,
                                                     BorderLayout.WEST);
  }

  private void _initForkPointPanel()
  {
    forkPointPaneWrapper = new ForkPointPaneWrapper(mergeDiff, editorKitObservable);
    forkPointPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    DiffPanelModel forkPointYoursModel = new DiffPanelModel(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges().getChangeChunks(),
                                                            IFileChangeChunk::getALines,
                                                            pFileChangeChunk -> "",
                                                            IFileChangeChunk::getAStart,
                                                            IFileChangeChunk::getAEnd);
    DiffPanelModel forkPointTheirsModel = new DiffPanelModel(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS).getFileChanges().getChangeChunks(),
                                                             IFileChangeChunk::getALines,
                                                             pFileChangeChunk -> "",
                                                             IFileChangeChunk::getAStart,
                                                             IFileChangeChunk::getAEnd);
    leftForkPointLineNumPanel = forkPointPaneWrapper.getPane().addLineNumPanel(forkPointYoursModel, BorderLayout.WEST);
    rightForkPointLineNumPanel = forkPointPaneWrapper.getPane().addLineNumPanel(forkPointTheirsModel, BorderLayout.EAST);
  }

  @Override
  public void discard()
  {
    yoursPaneWrapper.discard();
    theirsPaneWrapper.discard();
    forkPointPaneWrapper.discard();
  }
}
