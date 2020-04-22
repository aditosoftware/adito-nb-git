package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EConflictSide;
import de.adito.git.api.data.diff.IMergeData;
import de.adito.git.gui.Constants;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.LeftSideVSBScrollPaneLayout;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.LineNumbersColorModel;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.DiffPaneWrapper;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.ForkPointPaneWrapper;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.IPaneWrapper;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.swing.SynchronizedBoundedRangeModel;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Point;
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

  private static final String FORKPOINT_MODEL_KEY = "forkPointModel";
  private final IIconLoader iconLoader;
  private final IMergeData mergeDiff;
  private final ImageIcon acceptYoursIcon;
  private final ImageIcon acceptTheirsIcon;
  private final ImageIcon discardIcon;
  private final Subject<Optional<EditorKit>> editorKitObservable;
  private IDiffPaneUtil.ScrollBarCoupling yoursCoupling;
  private IDiffPaneUtil.ScrollBarCoupling theirsCoupling;
  private DiffPaneWrapper yoursPaneWrapper;
  private ForkPointPaneWrapper forkPointPaneWrapper;
  private DiffPaneWrapper theirsPaneWrapper;
  private LineNumbersColorModel leftForkPointLineNumColorModel;
  private LineNumbersColorModel rightForkPointLineNumColorModel;

  public MergePanel(IIconLoader pIconLoader, IMergeData pMergeDiff, ImageIcon pAcceptYoursIcon, ImageIcon pAcceptTheirsIcon, ImageIcon pDiscardIcon,
                    IEditorKitProvider pEditorKitProvider)
  {
    iconLoader = pIconLoader;
    mergeDiff = pMergeDiff;
    acceptYoursIcon = pAcceptYoursIcon;
    acceptTheirsIcon = pAcceptTheirsIcon;
    discardIcon = pDiscardIcon;
    editorKitObservable = BehaviorSubject.createDefault(Optional.empty());
    _initForkPointPanel();
    _initYoursPanel();
    _initTheirsPanel();
    _initGui();
    yoursCoupling = IDiffPaneUtil.synchronize(forkPointPaneWrapper, FORKPOINT_MODEL_KEY, yoursPaneWrapper, "yoursPane",
                                              Observable.just(Optional.of(pMergeDiff.getDiff(EConflictSide.YOURS))));
    theirsCoupling = IDiffPaneUtil.synchronize(forkPointPaneWrapper, FORKPOINT_MODEL_KEY, theirsPaneWrapper, "theirsPane",
                                               Observable.just(Optional.of(pMergeDiff.getDiff(EConflictSide.THEIRS))));
    editorKitObservable.onNext(Optional.of(Optional.ofNullable(pMergeDiff.getDiff(EConflictSide.YOURS).getFileHeader().getAbsoluteFilePath())
                                               .map(pEditorKitProvider::getEditorKit)
                                               .orElseGet(() -> pEditorKitProvider.getEditorKitForContentType("text/plain"))));
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
                                 forkPointPaneWrapper.getScrollPane().getHorizontalScrollBar().getModel(),
                                 yoursPaneWrapper.getScrollPane().getHorizontalScrollBar().getModel())
    );
    add(yoursTheirsPanel, BorderLayout.NORTH);
  }

  private void _initYoursPanel()
  {
    LineNumbersColorModel[] lineNumColorModels = new LineNumbersColorModel[2];
    DiffPanelModel yoursModel = new DiffPanelModel(mergeDiff.getDiff(EConflictSide.YOURS).getDiffTextChangeObservable(), EChangeSide.NEW)
        .setDoOnAccept(pChangeDelta -> mergeDiff.acceptDelta(pChangeDelta, EConflictSide.YOURS))
        .setDoOnDiscard(pChangeDelta -> mergeDiff.discardChange(pChangeDelta, EConflictSide.YOURS));
    yoursPaneWrapper = new DiffPaneWrapper(yoursModel, editorKitObservable);
    yoursPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    yoursPaneWrapper.getScrollPane().setLayout(new LeftSideVSBScrollPaneLayout());

    // Neccessary for the left ChoiceButtonPanel, but should not be added to the Layout
    LineNumbersColorModel temp = yoursPaneWrapper.getPane().createLineNumberColorModel(yoursModel, 1);
    // index 0 because the lineNumPanel is of the left-most panel, and thus to the left to the ChoiceButtonPanel
    lineNumColorModels[0] = yoursPaneWrapper.getPane().createLineNumberColorModel(yoursModel, 0);
    lineNumColorModels[1] = leftForkPointLineNumColorModel;

    yoursPaneWrapper.getPane().addChoiceButtonPanel(yoursModel, acceptYoursIcon, discardIcon, new LineNumbersColorModel[]{temp, lineNumColorModels[0]},
                                                    BorderLayout.EAST);
    yoursPaneWrapper.getPane().addLineNumPanel(lineNumColorModels[0], yoursModel, BorderLayout.EAST);
    yoursPaneWrapper.getPane().addChoiceButtonPanel(yoursModel, null, null,
                                                    lineNumColorModels, BorderLayout.EAST);
  }

  private void _initTheirsPanel()
  {
    LineNumbersColorModel[] lineNumPanels = new LineNumbersColorModel[2];
    DiffPanelModel theirsModel = new DiffPanelModel(mergeDiff.getDiff(EConflictSide.THEIRS).getDiffTextChangeObservable(), EChangeSide.NEW)
        .setDoOnAccept(pChangeDelta -> mergeDiff.acceptDelta(pChangeDelta, EConflictSide.THEIRS))
        .setDoOnDiscard(pChangeDelta -> mergeDiff.discardChange(pChangeDelta, EConflictSide.THEIRS));
    theirsPaneWrapper = new DiffPaneWrapper(theirsModel, editorKitObservable);
    theirsPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);

    // Neccessary for the right ChoiceButtonPanel, but should not be added to the Layout
    LineNumbersColorModel temp = theirsPaneWrapper.getPane().createLineNumberColorModel(theirsModel, 0);
    // index 1 because the lineNumPanel is of the right-most panel, and thus to the right to the ChoiceButtonPanel
    lineNumPanels[1] = theirsPaneWrapper.getPane().createLineNumberColorModel(theirsModel, 1);
    lineNumPanels[0] = rightForkPointLineNumColorModel;

    theirsPaneWrapper.getPane().addChoiceButtonPanel(theirsModel, acceptTheirsIcon, discardIcon, new LineNumbersColorModel[]{temp, lineNumPanels[1]},
                                                    BorderLayout.WEST);
    theirsPaneWrapper.getPane().addLineNumPanel(lineNumPanels[1], theirsModel, BorderLayout.WEST);
    theirsPaneWrapper.getPane().addChoiceButtonPanel(theirsModel, null, null,
                                                     lineNumPanels, BorderLayout.WEST);
  }

  private void _initForkPointPanel()
  {
    forkPointPaneWrapper = new ForkPointPaneWrapper(mergeDiff, editorKitObservable);
    forkPointPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    DiffPanelModel forkPointYoursModel = new DiffPanelModel(mergeDiff.getDiff(EConflictSide.YOURS).getDiffTextChangeObservable(), EChangeSide.OLD);
    DiffPanelModel forkPointTheirsModel = new DiffPanelModel(mergeDiff.getDiff(EConflictSide.THEIRS).getDiffTextChangeObservable(), EChangeSide.OLD);
    leftForkPointLineNumColorModel = forkPointPaneWrapper.getPane().addLineNumPanel(forkPointYoursModel, BorderLayout.WEST, 1);
    rightForkPointLineNumColorModel = forkPointPaneWrapper.getPane().addLineNumPanel(forkPointTheirsModel, BorderLayout.EAST, 0);
  }

  @NotNull
  public List<Action> getActions()
  {
    SynchronizedBoundedRangeModel forkPointModel = theirsCoupling.getModel(FORKPOINT_MODEL_KEY);
    return List.of(
        new EnhancedAbstractAction("", iconLoader.getIcon(Constants.NEXT_OCCURRENCE), "next change", e -> {
          if (yoursPaneWrapper.isEditorFocusOwner())
          {
            chunkMove(forkPointPaneWrapper, theirsPaneWrapper, forkPointModel, yoursPaneWrapper::moveCaretToNextChunk);
          }
          else
          {
            chunkMove(forkPointPaneWrapper, yoursPaneWrapper, forkPointModel, theirsPaneWrapper::moveCaretToNextChunk);
          }
        }),
        new EnhancedAbstractAction("", iconLoader.getIcon(Constants.PREVIOUS_OCCURRENCE), "previous change", e -> {
          if (yoursPaneWrapper.isEditorFocusOwner())
          {
            chunkMove(forkPointPaneWrapper, theirsPaneWrapper, forkPointModel, yoursPaneWrapper::moveCaretToPreviousChunk);
          }
          else
          {
            chunkMove(forkPointPaneWrapper, yoursPaneWrapper, forkPointModel, theirsPaneWrapper::moveCaretToPreviousChunk);
          }
        })
    );
  }

  /**
   * @param pForkPoint    PaneWrapper that should get its caret set
   * @param pOtherSide    PaneWrapper representing the "opposite" side of the diff
   * @param pRangeModel   SynchronizedBoundedRangeModel of the forkPoint that contains both the yours and theirs scrollBars
   * @param pMoveFunction function to be called for actually moving the caret
   */
  private void chunkMove(IPaneWrapper pForkPoint, DiffPaneWrapper pOtherSide, SynchronizedBoundedRangeModel pRangeModel, Consumer<JEditorPane> pMoveFunction)
  {
    pMoveFunction.accept(pForkPoint.getEditorPane());
    if (pRangeModel != null)
      pOtherSide.getEditorPane().getCaret().setDot(getOffsetAtPosition(pOtherSide.getEditorPane(),
                                                                       (int) pRangeModel.getMappedHeight(pOtherSide.getScrollPane().getVerticalScrollBar(),
                                                                                                         -pOtherSide.getEditorPane().getFont().getSize())));
  }

  /**
   * queries the ui of the textComponent for the offset in the text for the given view position
   * basically "y-position on screen -> corresponding offset at start of line in model"
   *
   * @param pTextComponent JTextComponent
   * @param pPosition      y coordinate in the view space
   * @return offset at start of line
   */
  private int getOffsetAtPosition(JTextComponent pTextComponent, int pPosition)
  {
    return pTextComponent.getUI().viewToModel(pTextComponent, new Point(0, pPosition));
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
