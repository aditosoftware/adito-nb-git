package de.adito.git.gui.dialogs.panels.BaseDiffPanel;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.Constants;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.DiffPaneWrapper;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.ForkPointPaneWrapper;

import javax.swing.*;
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
  private DiffPaneWrapper yoursPaneWrapper;
  private ForkPointPaneWrapper forkPointPaneWrapper;
  private DiffPaneWrapper theirsPaneWrapper;

  public MergePanel(IMergeDiff pMergeDiff, ImageIcon pAcceptYoursIcon, ImageIcon pAcceptTheirsIcon, ImageIcon pDiscardIcon)
  {
    mergeDiff = pMergeDiff;
    acceptYoursIcon = pAcceptYoursIcon;
    acceptTheirsIcon = pAcceptTheirsIcon;
    discardIcon = pDiscardIcon;
    _initYoursPanel();
    _initTheirsPanel();
    _initForkPointPanel();
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
    DiffPanelModel yoursModel = new DiffPanelModel(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges().getChangeChunks(),
                                                   IFileChangeChunk::getBLines, IFileChangeChunk::getBParityLines,
                                                   IFileChangeChunk::getBStart, IFileChangeChunk::getBEnd);
    yoursPaneWrapper = new DiffPaneWrapper(yoursModel);
    yoursPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    yoursPaneWrapper.getScrollPane().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    yoursPaneWrapper.getPane().addLineNumPanel(yoursModel, BorderLayout.EAST);
    yoursPaneWrapper.getPane().addChoiceButtonPanel(yoursModel, acceptYoursIcon, discardIcon,
                                                    pChangeChunk -> mergeDiff.acceptChunk(pChangeChunk, IMergeDiff.CONFLICT_SIDE.YOURS),
                                                    pChangeChunk -> mergeDiff.discardChange(pChangeChunk, IMergeDiff.CONFLICT_SIDE.YOURS),
                                                    BorderLayout.EAST);
  }

  private void _initTheirsPanel()
  {
    DiffPanelModel theirsModel = new DiffPanelModel(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS).getFileChanges().getChangeChunks(),
                                                    IFileChangeChunk::getBLines, IFileChangeChunk::getBParityLines,
                                                    IFileChangeChunk::getBStart, IFileChangeChunk::getBEnd);
    theirsPaneWrapper = new DiffPaneWrapper(theirsModel);
    theirsPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    theirsPaneWrapper.getPane().addLineNumPanel(theirsModel, BorderLayout.WEST);
    theirsPaneWrapper.getPane().addChoiceButtonPanel(theirsModel, acceptTheirsIcon, discardIcon,
                                                     pChangeChunk -> mergeDiff.acceptChunk(pChangeChunk, IMergeDiff.CONFLICT_SIDE.THEIRS),
                                                     pChangeChunk -> mergeDiff.discardChange(pChangeChunk, IMergeDiff.CONFLICT_SIDE.THEIRS),
                                                     BorderLayout.WEST);
  }

  private void _initForkPointPanel()
  {
    forkPointPaneWrapper = new ForkPointPaneWrapper(mergeDiff);
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
    forkPointPaneWrapper.getPane().addLineNumPanel(forkPointYoursModel, BorderLayout.WEST);
    forkPointPaneWrapper.getPane().addLineNumPanel(forkPointTheirsModel, BorderLayout.EAST);
  }

  @Override
  public void discard()
  {
    yoursPaneWrapper.discard();
    theirsPaneWrapper.discard();
    forkPointPaneWrapper.discard();
  }
}
