package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.Constants;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.LineNumbersColorModel;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.DiffPaneWrapper;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.ForkPointPaneWrapper;
import de.adito.git.gui.icon.IIconLoader;
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
  private LineNumbersColorModel leftForkPointLineNumColorModel;
  private LineNumbersColorModel rightForkPointLineNumColorModel;

  public MergePanel(IIconLoader pIconLoader, IMergeDiff pMergeDiff, ImageIcon pAcceptYoursIcon, ImageIcon pAcceptTheirsIcon, ImageIcon pDiscardIcon,
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
    _initGui(pIconLoader);
  }

  private void _initGui(IIconLoader pIconLoader)
  {
    setLayout(new BorderLayout());
    JSplitPane forkMergeSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, forkPointPaneWrapper.getPane(), theirsPaneWrapper.getPane());
    JSplitPane threeWayPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, yoursPaneWrapper.getPane(), forkMergeSplit);
    // 0.5 so the initial split is equal. For perfect feel on resizing set to 1, this would make the right pane almost invisible at the start though
    forkMergeSplit.setResizeWeight(0.5);
    // 0.33 because the right side contains two sub-windows, the left only one
    threeWayPane.setResizeWeight(0.33);
    add(threeWayPane, BorderLayout.CENTER);
    add(_initToolBar(pIconLoader), BorderLayout.NORTH);
  }

  private void _initYoursPanel()
  {
    LineNumbersColorModel[] lineNumColorModels = new LineNumbersColorModel[2];
    DiffPanelModel yoursModel = new DiffPanelModel(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges().getChangeChunks(),
                                                   EChangeSide.NEW)
        .setDoOnAccept(pChangeChunk -> mergeDiff.acceptChunk(pChangeChunk, IMergeDiff.CONFLICT_SIDE.YOURS))
        .setDoOnDiscard(pChangeChunk -> mergeDiff.discardChange(pChangeChunk, IMergeDiff.CONFLICT_SIDE.YOURS));
    yoursPaneWrapper = new DiffPaneWrapper(yoursModel, editorKitObservable);
    yoursPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    yoursPaneWrapper.getScrollPane().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    // index 0 because the lineNumPanel is of the left-most panel, and thus to the left to the ChoiceButtonPanel
    lineNumColorModels[0] = yoursPaneWrapper.getPane().addLineNumPanel(yoursModel, BorderLayout.EAST, 0);
    lineNumColorModels[1] = leftForkPointLineNumColorModel;
    yoursPaneWrapper.getPane().addChoiceButtonPanel(yoursModel, acceptYoursIcon, discardIcon,
                                                    lineNumColorModels,
                                                    BorderLayout.EAST);
  }

  private void _initTheirsPanel()
  {
    LineNumbersColorModel[] lineNumPanels = new LineNumbersColorModel[2];
    DiffPanelModel theirsModel = new DiffPanelModel(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS).getFileChanges().getChangeChunks(),
                                                    EChangeSide.NEW)
        .setDoOnAccept(pChangeChunk -> mergeDiff.acceptChunk(pChangeChunk, IMergeDiff.CONFLICT_SIDE.THEIRS))
        .setDoOnDiscard(pChangeChunk -> mergeDiff.discardChange(pChangeChunk, IMergeDiff.CONFLICT_SIDE.THEIRS));
    theirsPaneWrapper = new DiffPaneWrapper(theirsModel, editorKitObservable);
    theirsPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    // index 1 because the lineNumPanel is of the right-most panel, and thus to the right to the ChoiceButtonPanel
    lineNumPanels[1] = theirsPaneWrapper.getPane().addLineNumPanel(theirsModel, BorderLayout.WEST, 1);
    lineNumPanels[0] = rightForkPointLineNumColorModel;
    theirsPaneWrapper.getPane().addChoiceButtonPanel(theirsModel, acceptTheirsIcon, discardIcon,
                                                     lineNumPanels,
                                                     BorderLayout.WEST);
  }

  private void _initForkPointPanel()
  {
    forkPointPaneWrapper = new ForkPointPaneWrapper(mergeDiff, editorKitObservable);
    forkPointPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    DiffPanelModel forkPointYoursModel = new DiffPanelModel(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges().getChangeChunks(),
                                                            EChangeSide.OLD);
    DiffPanelModel forkPointTheirsModel = new DiffPanelModel(mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS).getFileChanges().getChangeChunks(),
                                                             EChangeSide.OLD);
    leftForkPointLineNumColorModel = forkPointPaneWrapper.getPane().addLineNumPanel(forkPointYoursModel, BorderLayout.WEST, 1);
    rightForkPointLineNumColorModel = forkPointPaneWrapper.getPane().addLineNumPanel(forkPointTheirsModel, BorderLayout.EAST, 0);
  }

  private JToolBar _initToolBar(IIconLoader pIconLoader)
  {
    JButton nextButton = new JButton(pIconLoader.getIcon(Constants.NEXT_OCCURRENCE));
    nextButton.setFocusable(false);
    nextButton.addActionListener(e -> {
      if (yoursPaneWrapper.isEditorFocusOwner())
        yoursPaneWrapper.moveCaretToNextChunk();
      else
        theirsPaneWrapper.moveCaretToNextChunk();
    });
    JButton previousButton = new JButton(pIconLoader.getIcon(Constants.PREVIOUS_OCCURRENCE));
    previousButton.setFocusable(false);
    previousButton.addActionListener(e -> {
      if (yoursPaneWrapper.isEditorFocusOwner())
        yoursPaneWrapper.moveCaretToPreviousChunk();
      else
        theirsPaneWrapper.moveCaretToPreviousChunk();
    });
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(nextButton);
    toolBar.add(previousButton);
    return toolBar;
  }

  @Override
  public void discard()
  {
    yoursPaneWrapper.discard();
    theirsPaneWrapper.discard();
    forkPointPaneWrapper.discard();
  }
}
