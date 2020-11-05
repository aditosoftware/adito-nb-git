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
import de.adito.git.gui.swing.SwingUtil;
import de.adito.git.gui.swing.SynchronizedBoundedRangeModel;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  private final IDiffPaneUtil.ScrollBarCoupling yoursCoupling;
  private final IDiffPaneUtil.ScrollBarCoupling theirsCoupling;
  private DiffPaneWrapper yoursPaneWrapper;
  private ForkPointPaneWrapper forkPointPaneWrapper;
  private DiffPaneWrapper theirsPaneWrapper;
  private LineNumbersColorModel leftForkPointLineNumColorModel;
  private LineNumbersColorModel rightForkPointLineNumColorModel;

  public MergePanel(@NotNull IIconLoader pIconLoader, @NotNull IMergeData pMergeDiff, @NotNull String pYoursOrigin, @NotNull String pTheirsOrigin,
                    @Nullable ImageIcon pAcceptYoursIcon, @Nullable ImageIcon pAcceptTheirsIcon, @Nullable ImageIcon pDiscardIcon,
                    @NotNull IEditorKitProvider pEditorKitProvider)
  {
    iconLoader = pIconLoader;
    mergeDiff = pMergeDiff;
    acceptYoursIcon = pAcceptYoursIcon;
    acceptTheirsIcon = pAcceptTheirsIcon;
    discardIcon = pDiscardIcon;
    editorKitObservable = BehaviorSubject.createDefault(Optional.empty());
    _initGui(pYoursOrigin, pTheirsOrigin);
    MouseFirstActionObservableWrapper mouseFirstActionObservableWrapper = new MouseFirstActionObservableWrapper(yoursPaneWrapper.getEditorPane(),
                                                                                                                forkPointPaneWrapper.getEditorPane(),
                                                                                                                theirsPaneWrapper.getEditorPane());
    yoursCoupling = IDiffPaneUtil.synchronize(forkPointPaneWrapper, FORKPOINT_MODEL_KEY, yoursPaneWrapper, "yoursPane",
                                              mouseFirstActionObservableWrapper.getObservable(), Observable.just(Optional.of(pMergeDiff.getDiff(EConflictSide.YOURS))));
    theirsCoupling = IDiffPaneUtil.synchronize(forkPointPaneWrapper, FORKPOINT_MODEL_KEY, theirsPaneWrapper, "theirsPane",
                                               mouseFirstActionObservableWrapper.getObservable(), Observable.just(Optional.of(pMergeDiff.getDiff(EConflictSide.THEIRS))));
    editorKitObservable.onNext(Optional.of(Optional.ofNullable(pMergeDiff.getDiff(EConflictSide.YOURS).getFileHeader().getAbsoluteFilePath())
                                               .map(pEditorKitProvider::getEditorKit)
                                               .orElseGet(() -> pEditorKitProvider.getEditorKitForContentType("text/plain"))));
  }

  private void _initGui(@NotNull String pYoursOrigin, @NotNull String pTheirsOrigin)
  {
    forkPointPaneWrapper = new ForkPointPaneWrapper(mergeDiff, editorKitObservable);
    DiffPanelModel yoursModel = new DiffPanelModel(mergeDiff.getDiff(EConflictSide.YOURS).getDiffTextChangeObservable(), EChangeSide.NEW)
        .setDoOnAccept(pChangeDelta -> mergeDiff.acceptDelta(pChangeDelta, EConflictSide.YOURS))
        .setDoOnDiscard(pChangeDelta -> mergeDiff.discardChange(pChangeDelta, EConflictSide.YOURS));
    yoursPaneWrapper = new DiffPaneWrapper(yoursModel, null, SwingConstants.RIGHT, editorKitObservable);
    DiffPanelModel theirsModel = new DiffPanelModel(mergeDiff.getDiff(EConflictSide.THEIRS).getDiffTextChangeObservable(), EChangeSide.NEW)
        .setDoOnAccept(pChangeDelta -> mergeDiff.acceptDelta(pChangeDelta, EConflictSide.THEIRS))
        .setDoOnDiscard(pChangeDelta -> mergeDiff.discardChange(pChangeDelta, EConflictSide.THEIRS));
    theirsPaneWrapper = new DiffPaneWrapper(theirsModel, null, SwingConstants.LEFT, editorKitObservable);
    // This observable has a default value and then only fires once, when the first mouse action occurs on any of the editorPanes. This is one of the better
    // (not good, mind you) mechanism to make sure the heights are properly calculated by the pane when they are used
    MouseFirstActionObservableWrapper mouseFirstActionObservableWrapper = new MouseFirstActionObservableWrapper(yoursPaneWrapper.getEditorPane(),
                                                                                                                theirsPaneWrapper.getEditorPane(),
                                                                                                                forkPointPaneWrapper.getEditorPane());
    _initForkPointPanel(mouseFirstActionObservableWrapper.getObservable());
    _initYoursPanel(yoursModel, mouseFirstActionObservableWrapper.getObservable());
    _initTheirsPanel(theirsModel, mouseFirstActionObservableWrapper.getObservable());
    setLayout(new BorderLayout());
    JSplitPane forkMergeSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, forkPointPaneWrapper.getPaneContainer(), theirsPaneWrapper.getPaneContainer());
    forkMergeSplit.setBorder(new EmptyBorder(0, 0, 0, 0));
    JSplitPane threeWayPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, yoursPaneWrapper.getPaneContainer(), forkMergeSplit);
    threeWayPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    // 0.5 so the initial split is equal. For perfect feel on resizing set to 1, this would make the right pane almost invisible at the start though
    forkMergeSplit.setResizeWeight(0.5);
    // 0.33 because the right side contains two sub-windows, the left only one
    threeWayPane.setResizeWeight(0.33);
    add(threeWayPane, BorderLayout.CENTER);
    JPanel yoursTheirsPanel = new JPanel(new BorderLayout());
    yoursTheirsPanel.setBorder(new EmptyBorder(3, 16, 3, 16));
    yoursTheirsPanel.add(new JLabel(pYoursOrigin), BorderLayout.WEST);
    yoursTheirsPanel.add(new JLabel(pTheirsOrigin), BorderLayout.EAST);
    // couple horizontal scrollbars
    IDiffPaneUtil.bridge(List.of(theirsPaneWrapper.getScrollPane().getHorizontalScrollBar().getModel(),
                                 forkPointPaneWrapper.getScrollPane().getHorizontalScrollBar().getModel(),
                                 yoursPaneWrapper.getScrollPane().getHorizontalScrollBar().getModel())
    );
    SwingUtil.invokeASAP(() -> yoursPaneWrapper.getScrollPane().getHorizontalScrollBar().setValue(0));
    add(yoursTheirsPanel, BorderLayout.NORTH);
  }

  private void _initYoursPanel(DiffPanelModel pYoursModel, Observable<Optional<Object>> pObservable)
  {
    LineNumbersColorModel[] lineNumColorModels = new LineNumbersColorModel[2];

    yoursPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    yoursPaneWrapper.getScrollPane().setLayout(new LeftSideVSBScrollPaneLayout());

    // Neccessary for the left ChoiceButtonPanel, but should not be added to the Layout
    LineNumbersColorModel temp = yoursPaneWrapper.getPaneContainer().createLineNumberColorModel(pYoursModel, pObservable, 1);
    // index 0 because the lineNumPanel is of the left-most panel, and thus to the left to the ChoiceButtonPanel
    lineNumColorModels[0] = yoursPaneWrapper.getPaneContainer().createLineNumberColorModel(pYoursModel, pObservable, 0);
    lineNumColorModels[1] = leftForkPointLineNumColorModel;

    yoursPaneWrapper.getPaneContainer().addChoiceButtonPanel(pYoursModel, acceptYoursIcon, discardIcon, new LineNumbersColorModel[]{temp, lineNumColorModels[0]},
                                                             BorderLayout.EAST);
    yoursPaneWrapper.getPaneContainer().addLineNumPanel(lineNumColorModels[0], pYoursModel, BorderLayout.EAST);
    yoursPaneWrapper.getPaneContainer().addChoiceButtonPanel(pYoursModel, null, null,
                                                             lineNumColorModels, BorderLayout.EAST);
  }

  private void _initTheirsPanel(DiffPanelModel pTheirsModel, Observable<Optional<Object>> pObservable)
  {
    LineNumbersColorModel[] lineNumPanels = new LineNumbersColorModel[2];
    theirsPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);

    // Neccessary for the right ChoiceButtonPanel, but should not be added to the Layout
    LineNumbersColorModel temp = theirsPaneWrapper.getPaneContainer().createLineNumberColorModel(pTheirsModel, pObservable, 0);
    // index 1 because the lineNumPanel is of the right-most panel, and thus to the right to the ChoiceButtonPanel
    lineNumPanels[1] = theirsPaneWrapper.getPaneContainer().createLineNumberColorModel(pTheirsModel, pObservable, 1);
    lineNumPanels[0] = rightForkPointLineNumColorModel;

    theirsPaneWrapper.getPaneContainer().addChoiceButtonPanel(pTheirsModel, acceptTheirsIcon, discardIcon, new LineNumbersColorModel[]{temp, lineNumPanels[1]},
                                                              BorderLayout.WEST);
    theirsPaneWrapper.getPaneContainer().addLineNumPanel(lineNumPanels[1], pTheirsModel, BorderLayout.WEST);
    theirsPaneWrapper.getPaneContainer().addChoiceButtonPanel(pTheirsModel, null, null,
                                                              lineNumPanels, BorderLayout.WEST);
  }

  private void _initForkPointPanel(Observable<Optional<Object>> pObservable)
  {
    forkPointPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    DiffPanelModel forkPointYoursModel = new DiffPanelModel(mergeDiff.getDiff(EConflictSide.YOURS).getDiffTextChangeObservable(), EChangeSide.OLD);
    DiffPanelModel forkPointTheirsModel = new DiffPanelModel(mergeDiff.getDiff(EConflictSide.THEIRS).getDiffTextChangeObservable(), EChangeSide.OLD);
    leftForkPointLineNumColorModel = forkPointPaneWrapper.getPaneContainer().addLineNumPanel(forkPointYoursModel, pObservable, BorderLayout.WEST, 1);
    rightForkPointLineNumColorModel = forkPointPaneWrapper.getPaneContainer().addLineNumPanel(forkPointTheirsModel, pObservable, BorderLayout.EAST, 0);
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
