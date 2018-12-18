package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.panels.TextPanes.DiffPaneWrapper;
import de.adito.git.gui.dialogs.panels.TextPanes.ForkPointPaneWrapper;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.Optional;

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
  private BaseDiffPanel yoursPanel;
  private BaseDiffPanel forkPointPanel;
  private BaseDiffPanel theirsPanel;

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
    JSplitPane forkToMergeSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, forkPointPanel, theirsPanel);
    JSplitPane threeWayPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, yoursPanel, forkToMergeSplit);
    // 0.5 so the initial split is equal. For perfect feel on resizing set to 1, this would make the right pane almost invisible at the start though
    forkToMergeSplit.setResizeWeight(0.5);
    // 0.33 because the right side contains two sub-windows, the left only one
    threeWayPane.setResizeWeight(0.33);
    add(threeWayPane);
  }

  private void _initYoursPanel()
  {
    DiffPanelModel yoursModel = new DiffPanelModel(Observable.just(Optional.of(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS))),
                                                   pFileChangeChunk -> pFileChangeChunk.getBEnd() - pFileChangeChunk.getBStart(),
                                                   IFileChangeChunk::getBLines, IFileChangeChunk::getBParityLines
    );
    DiffPaneWrapper diffPaneWrapper = new DiffPaneWrapper(yoursModel);
    yoursPanel = new BaseDiffPanel(diffPaneWrapper.getPane(), diffPaneWrapper.getTextPane());
    yoursPanel.addLineNumPanel(yoursModel, BorderLayout.EAST);
    yoursPanel.addChoiceButtonPanel(yoursModel, discardIcon, acceptYoursIcon,
                                    pChangeChunk -> mergeDiff.discardChange(pChangeChunk, IMergeDiff.CONFLICT_SIDE.YOURS),
                                    pChangeChunk -> mergeDiff.acceptChunk(pChangeChunk, IMergeDiff.CONFLICT_SIDE.YOURS),
                                    BorderLayout.EAST);
  }

  private void _initTheirsPanel()
  {
    DiffPanelModel theirsModel = new DiffPanelModel(Observable.just(Optional.of(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS))),
                                                    pFileChangeChunk -> pFileChangeChunk.getBEnd() - pFileChangeChunk.getBStart(),
                                                    IFileChangeChunk::getBLines, IFileChangeChunk::getBParityLines);
    DiffPaneWrapper diffPaneWrapper = new DiffPaneWrapper(theirsModel);
    theirsPanel = new BaseDiffPanel(diffPaneWrapper.getPane(), diffPaneWrapper.getTextPane());
    theirsPanel.addLineNumPanel(theirsModel, BorderLayout.WEST);
    theirsPanel.addChoiceButtonPanel(theirsModel, discardIcon, acceptTheirsIcon,
                                     pChangeChunk -> mergeDiff.discardChange(pChangeChunk, IMergeDiff.CONFLICT_SIDE.THEIRS),
                                     pChangeChunk -> mergeDiff.acceptChunk(pChangeChunk, IMergeDiff.CONFLICT_SIDE.THEIRS),
                                     BorderLayout.WEST);
  }

  private void _initForkPointPanel()
  {
    ForkPointPaneWrapper forkPointPaneWrapper = new ForkPointPaneWrapper(mergeDiff);
    forkPointPanel = new BaseDiffPanel(forkPointPaneWrapper.getPane(), forkPointPaneWrapper.getTextPane());
    DiffPanelModel forkPointYoursModel = new DiffPanelModel(Observable.just(Optional.of(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS))),
                                                            pFileChangeChunk -> pFileChangeChunk.getAEnd() - pFileChangeChunk.getAStart(),
                                                            IFileChangeChunk::getALines, IFileChangeChunk::getAParityLines);
    DiffPanelModel forkPointTheirsModel = new DiffPanelModel(Observable.just(Optional.of(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS))),
                                                             pFileChangeChunk -> pFileChangeChunk.getAEnd() - pFileChangeChunk.getAStart(),
                                                             IFileChangeChunk::getALines, IFileChangeChunk::getAParityLines);
    forkPointPanel.addLineNumPanel(forkPointYoursModel, BorderLayout.WEST);
    forkPointPanel.addLineNumPanel(forkPointTheirsModel, BorderLayout.EAST);
  }

  @Override
  public void discard()
  {
    forkPointPanel.discard();
    yoursPanel.discard();
    theirsPanel.discard();
  }
}
