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
import de.adito.git.impl.util.DifferentialScrollBarCoupling;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.EditorKit;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Class that handles the layout of the merge dialog and creates the elements in that form the dialog
 *
 * @author m.kaspera 12.11.2018
 */
public class MergePanel extends JPanel implements IDiscardable
{

  private final IIconLoader iconLoader;
  private final IMergeDiff mergeDiff;
  private final ImageIcon acceptYoursIcon;
  private final ImageIcon acceptTheirsIcon;
  private final ImageIcon discardIcon;
  private final Observable<EditorKit> editorKitObservable;
  private DifferentialScrollBarCoupling yoursCoupling;
  private DifferentialScrollBarCoupling theirsCoupling;
  private DiffPaneWrapper yoursPaneWrapper;
  private ForkPointPaneWrapper forkPointPaneWrapper;
  private DiffPaneWrapper theirsPaneWrapper;
  private LineNumbersColorModel leftForkPointLineNumColorModel;
  private LineNumbersColorModel rightForkPointLineNumColorModel;

  public MergePanel(IIconLoader pIconLoader, IMergeDiff pMergeDiff, ImageIcon pAcceptYoursIcon, ImageIcon pAcceptTheirsIcon, ImageIcon pDiscardIcon,
                    IEditorKitProvider pEditorKitProvider)
  {
    iconLoader = pIconLoader;
    mergeDiff = pMergeDiff;
    acceptYoursIcon = pAcceptYoursIcon;
    acceptTheirsIcon = pAcceptTheirsIcon;
    discardIcon = pDiscardIcon;
    editorKitObservable = Observable.just(Optional.ofNullable(pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getAbsoluteFilePath()))
        .map(pPath -> pPath
            .map(pEditorKitProvider::getEditorKit)
            .orElseGet(() -> pEditorKitProvider.getEditorKitForContentType("text/plain")));
    _initForkPointPanel();
    _initYoursPanel();
    _initTheirsPanel();
    _initGui();
    yoursCoupling = IDiffPaneUtil.synchronize(forkPointPaneWrapper.getScrollPane(), yoursPaneWrapper.getScrollPane(),
                                              forkPointPaneWrapper.getEditorPane(), yoursPaneWrapper.getEditorPane(),
                                              Observable.just(Optional.of(pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS))));
    theirsCoupling = IDiffPaneUtil.synchronize(forkPointPaneWrapper.getScrollPane(), theirsPaneWrapper.getScrollPane(),
                                               forkPointPaneWrapper.getEditorPane(), theirsPaneWrapper.getEditorPane(),
                                               Observable.just(Optional.of(pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS))));
  }

  private void _initGui()
  {
    setLayout(new BorderLayout());
    JSplitPane forkMergeSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, forkPointPaneWrapper.getPane(), theirsPaneWrapper.getPane());
    forkMergeSplit.setBorder(new EmptyBorder(0, 0, 0, 0));
    JSplitPane threeWayPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, yoursPaneWrapper.getPane(), forkMergeSplit);
    threeWayPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    // 0.5 so the initial split is equal. For perfect feel on resizing set to 1, this would make the right pane almost invisible at the start though
    forkMergeSplit.setResizeWeight(0.5);
    // 0.33 because the right side contains two sub-windows, the left only one
    threeWayPane.setResizeWeight(0.33);
    add(threeWayPane, BorderLayout.CENTER);
    JPanel yoursTheirsPanel = new JPanel(new BorderLayout());
    yoursTheirsPanel.setBorder(new EmptyBorder(3, 16, 3, 16));
    yoursTheirsPanel.add(new JLabel("Your changes"), BorderLayout.WEST);
    yoursTheirsPanel.add(new JLabel("Their changes"), BorderLayout.EAST);
    // couple horizontal scrollbars
    IDiffPaneUtil.bridge(List.of(theirsPaneWrapper.getScrollPane().getHorizontalScrollBar().getModel(),
                                 forkPointPaneWrapper.getScrollPane().getHorizontalScrollBar().getModel()),
                         List.of(yoursPaneWrapper.getScrollPane().getHorizontalScrollBar().getModel()));
    add(yoursTheirsPanel, BorderLayout.NORTH);
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

  @NotNull
  public List<Action> getActions()
  {
    return List.of(
        new EnhancedAbstractAction("", iconLoader.getIcon(Constants.NEXT_OCCURRENCE), "next change", e -> {
          if (yoursPaneWrapper.isEditorFocusOwner())
            yoursPaneWrapper.moveCaretToNextChunk();
          else
            theirsPaneWrapper.moveCaretToNextChunk();
        }),
        new EnhancedAbstractAction("", iconLoader.getIcon(Constants.PREVIOUS_OCCURRENCE), "previous change", e -> {
          if (yoursPaneWrapper.isEditorFocusOwner())
            yoursPaneWrapper.moveCaretToPreviousChunk();
          else
            theirsPaneWrapper.moveCaretToPreviousChunk();
        })
    );
  }

  @Override
  public void discard()
  {
    yoursPaneWrapper.discard();
    theirsPaneWrapper.discard();
    forkPointPaneWrapper.discard();
    yoursCoupling.discard();
    theirsCoupling.discard();
  }

  /**
   * AbstractAction that also takes the short Description in the constructor
   */
  private static class EnhancedAbstractAction extends AbstractAction
  {

    private final Consumer<ActionEvent> doOnActionPerformed;

    EnhancedAbstractAction(String pTitle, ImageIcon pIcon, String pShortDescription, Consumer<ActionEvent> pDoOnActionPerformed)
    {
      super(pTitle, pIcon);
      doOnActionPerformed = pDoOnActionPerformed;
      putValue(SHORT_DESCRIPTION, pShortDescription);
    }

    @Override
    public void actionPerformed(ActionEvent pEvent)
    {
      doOnActionPerformed.accept(pEvent);
    }
  }
}
