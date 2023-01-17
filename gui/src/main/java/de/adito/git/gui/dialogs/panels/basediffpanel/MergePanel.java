package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.*;
import de.adito.git.gui.Constants;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.LeftSideVSBScrollPaneLayout;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.LineChangeMarkingModel;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.LineNumberModel;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.ViewLineChangeMarkingModel;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.DiffPaneWrapper;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.ForkPointPaneWrapper;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.IPaneWrapper;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.swing.SwingUtil;
import de.adito.git.gui.swing.SynchronizedBoundedRangeModel;
import de.adito.git.impl.data.diff.ConflictPair;
import de.adito.git.impl.data.diff.EConflictType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
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
  private final Subject<Optional<Object>> initHeightsObs;
  private final UseLeftChunkAction useLeftChunk;
  private final UseRightChunkAction useRightChunk;
  private final UseLeftThenRightAction useLeftThenRightChunk;
  private final UseRightThenLeftAction useRightThenLeftChunk;
  private DiffPaneWrapper yoursPaneWrapper;
  private ForkPointPaneWrapper forkPointPaneWrapper;
  private DiffPaneWrapper theirsPaneWrapper;
  private CaretMovedListener caretListener;

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
    useLeftChunk = new UseLeftChunkAction();
    useRightChunk = new UseRightChunkAction();
    useLeftThenRightChunk = new UseLeftThenRightAction();
    useRightThenLeftChunk = new UseRightThenLeftAction();
    _initGui(pYoursOrigin, pTheirsOrigin);
    initHeightsObs = BehaviorSubject.create();
    initHeightsObs.onNext(Optional.empty());
    yoursCoupling = IDiffPaneUtil.synchronize(forkPointPaneWrapper, FORKPOINT_MODEL_KEY, yoursPaneWrapper, "yoursPane",
                                              initHeightsObs, Observable.just(Optional.of(pMergeDiff.getDiff(EConflictSide.YOURS))));
    theirsCoupling = IDiffPaneUtil.synchronize(forkPointPaneWrapper, FORKPOINT_MODEL_KEY, theirsPaneWrapper, "theirsPane",
                                               initHeightsObs, Observable.just(Optional.of(pMergeDiff.getDiff(EConflictSide.THEIRS))));
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
    initModels(yoursModel, theirsModel);
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
    SwingUtil.invokeInEDT(() -> yoursPaneWrapper.getScrollPane().getHorizontalScrollBar().setValue(0));
    add(yoursTheirsPanel, BorderLayout.NORTH);
    caretListener = new CaretMovedListener();
    yoursPaneWrapper.getEditorPane().addCaretListener(caretListener);
    theirsPaneWrapper.getEditorPane().addCaretListener(caretListener);
    forkPointPaneWrapper.getEditorPane().addCaretListener(caretListener);
  }

  private void initModels(DiffPanelModel yoursModel, DiffPanelModel theirsModel)
  {
    LineNumberModel yourLineNumberModel = yoursPaneWrapper.createLineNumberModel();
    LineChangeMarkingModel yourLineChangeMarkingModel = new LineChangeMarkingModel(yourLineNumberModel, EChangeSide.NEW);
    ViewLineChangeMarkingModel yourViewChangeMarkingModel = new ViewLineChangeMarkingModel(yourLineChangeMarkingModel, yoursPaneWrapper.getScrollPane().getViewport());
    LineNumberModel theirLineNumberModel = theirsPaneWrapper.createLineNumberModel();
    LineChangeMarkingModel theirLineChangeMarkingModel = new LineChangeMarkingModel(theirLineNumberModel, EChangeSide.NEW);
    ViewLineChangeMarkingModel theirViewChangeMarkingModel = new ViewLineChangeMarkingModel(theirLineChangeMarkingModel, theirsPaneWrapper.getScrollPane().getViewport());
    LineNumberModel forkPointLineNumberModel = forkPointPaneWrapper.createLineNumberModel(EConflictSide.YOURS);
    LineNumberModel forkPointTheirLineNumberModel = forkPointPaneWrapper.createLineNumberModel(EConflictSide.THEIRS);
    LineChangeMarkingModel forkPointYourChangeMarkingModel = new LineChangeMarkingModel(forkPointLineNumberModel, EChangeSide.OLD);
    LineChangeMarkingModel forkPointTheirChangeMarkingModel = new LineChangeMarkingModel(forkPointTheirLineNumberModel, EChangeSide.OLD);
    ViewLineChangeMarkingModel forkPointYourViewChangeMarkingModel = new ViewLineChangeMarkingModel(forkPointYourChangeMarkingModel,
                                                                                                    forkPointPaneWrapper.getScrollPane().getViewport());
    ViewLineChangeMarkingModel forkPointTheirViewChangeMarkingModel = new ViewLineChangeMarkingModel(forkPointTheirChangeMarkingModel,
                                                                                                     forkPointPaneWrapper.getScrollPane().getViewport());
    _initYoursPanel(yoursModel, yourLineNumberModel, yourLineChangeMarkingModel, yourViewChangeMarkingModel, forkPointYourViewChangeMarkingModel);
    _initTheirsPanel(theirsModel, theirLineNumberModel, theirLineChangeMarkingModel, theirViewChangeMarkingModel, forkPointTheirViewChangeMarkingModel);
    _initForkPointPanel(forkPointLineNumberModel, forkPointTheirLineNumberModel, forkPointYourChangeMarkingModel, forkPointTheirChangeMarkingModel);
  }

  private void _initYoursPanel(@NotNull DiffPanelModel pYoursModel, @NotNull LineNumberModel pYourLineNumberModel, @NotNull LineChangeMarkingModel pYourChangeMarkingModel,
                               @NotNull ViewLineChangeMarkingModel pYourViewChangeMarkingModel, @NotNull ViewLineChangeMarkingModel pForkpointYourViewChangeMarkingModel)
  {
    yoursPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    yoursPaneWrapper.getScrollPane().setLayout(new LeftSideVSBScrollPaneLayout());

    yoursPaneWrapper.getPaneContainer().addChoiceButtonPanel(pYoursModel, pYourLineNumberModel, pYourViewChangeMarkingModel, pYourViewChangeMarkingModel,
                                                             acceptYoursIcon, discardIcon, BorderLayout.EAST);
    yoursPaneWrapper.getPaneContainer().addLineNumPanel(pYourLineNumberModel, pYourChangeMarkingModel, BorderLayout.EAST);
    // this is added to draw the "connecting" areas between yours and the forkPoint Panel
    yoursPaneWrapper.getPaneContainer().addChoiceButtonPanel(pYoursModel, pYourLineNumberModel, pYourViewChangeMarkingModel, pForkpointYourViewChangeMarkingModel,
                                                             null, null, BorderLayout.EAST);
  }

  private void _initTheirsPanel(@NotNull DiffPanelModel pTheirsModel, @NotNull LineNumberModel pTheirLineNumberModel, @NotNull LineChangeMarkingModel pTheirChangeMarkingModel,
                                @NotNull ViewLineChangeMarkingModel pTheirViewChangeMarkingModel, @NotNull ViewLineChangeMarkingModel pForkpointTheirViewChangeMarkingModel)
  {
    theirsPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);

    theirsPaneWrapper.getPaneContainer().addChoiceButtonPanel(pTheirsModel, pTheirLineNumberModel, pTheirViewChangeMarkingModel, pTheirViewChangeMarkingModel,
                                                              acceptTheirsIcon, discardIcon, BorderLayout.WEST);
    theirsPaneWrapper.getPaneContainer().addLineNumPanel(pTheirLineNumberModel, pTheirChangeMarkingModel, BorderLayout.WEST);
    // this is added to draw the "connecting" areas between theirs and the forkPoint Panel
    theirsPaneWrapper.getPaneContainer().addChoiceButtonPanel(pTheirsModel, pTheirLineNumberModel, pForkpointTheirViewChangeMarkingModel, pTheirViewChangeMarkingModel,
                                                              null, null, BorderLayout.WEST);
  }

  private void _initForkPointPanel(@NotNull LineNumberModel pForkpointLineNumberModel, @NotNull LineNumberModel pForkpointLineTheirNumberModel,
                                   @NotNull LineChangeMarkingModel pForkpointYourChangeMarkingModel,
                                   @NotNull LineChangeMarkingModel pForkpointTheirChangeMarkingModel)
  {
    forkPointPaneWrapper.getScrollPane().getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    forkPointPaneWrapper.getPaneContainer().addLineNumPanel(pForkpointLineNumberModel, pForkpointYourChangeMarkingModel, BorderLayout.WEST);
    forkPointPaneWrapper.getPaneContainer().addLineNumPanel(pForkpointLineTheirNumberModel, pForkpointTheirChangeMarkingModel, BorderLayout.EAST);
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
   * This list should contain the list of actions that resolve both sides of a conflict at once (by either accepting both sides in a certain order,
   * or accepting one side and discarding the other)
   *
   * @return List of Actions
   */
  @NotNull
  public List<MergeChunkAction> getMergeChunkActions()
  {
    return List.of(useLeftChunk, useRightChunk, useLeftThenRightChunk, useRightThenLeftChunk);
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
    yoursPaneWrapper.getEditorPane().removeCaretListener(caretListener);
    theirsPaneWrapper.getEditorPane().removeCaretListener(caretListener);
    forkPointPaneWrapper.getEditorPane().removeCaretListener(caretListener);
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

    @NotNull
    private final Consumer<ActionEvent> doOnActionPerformed;

    EnhancedAbstractAction(@Nullable String pTitle, @Nullable ImageIcon pIcon, @Nullable String pShortDescription, @NotNull Consumer<ActionEvent> pDoOnActionPerformed)
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

  /**
   * Listener that checks if the caret is positioned inside a conflicting delta, and enables or disables the MergeChunkActions accordingly
   */
  private class CaretMovedListener implements CaretListener
  {

    @Override
    public void caretUpdate(CaretEvent e)
    {
      IChangeDelta changeDelta = null;
      if (yoursPaneWrapper.isEditorFocusOwner())
      {
        changeDelta = yoursPaneWrapper.getCurrentChunk();
      }
      else if (theirsPaneWrapper.isEditorFocusOwner())
      {
        changeDelta = theirsPaneWrapper.getCurrentChunk();
      }
      for (MergeChunkAction pAction : getMergeChunkActions())
      {
        pAction.setCurrentDelta(changeDelta);
      }

    }
  }

  private class UseLeftChunkAction extends MergeChunkAction
  {

    public UseLeftChunkAction()
    {
      super(iconLoader.getIcon(Constants.ACCEPT_LEFT_CONFLICT));
      putValue(SHORT_DESCRIPTION, "Apply the Left Change and discard the Right Change");
    }

    @Override
    void actionPerformed0(@NotNull ConflictPair pConflictPair)
    {
      IChangeDelta yoursDelta = yoursPaneWrapper.getFileDiff().getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.YOURS));
      mergeDiff.acceptDelta(yoursDelta, EConflictSide.YOURS);
      IChangeDelta theirsDelta = theirsPaneWrapper.getFileDiff().getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.THEIRS));
      mergeDiff.discardChange(theirsDelta, EConflictSide.THEIRS);
    }
  }

  private class UseRightChunkAction extends MergeChunkAction
  {

    public UseRightChunkAction()
    {
      super(iconLoader.getIcon(Constants.ACCEPT_RIGHT_CONFLICT));
      putValue(SHORT_DESCRIPTION, "Apply the Right Change and discard the Left Change");
    }

    @Override
    void actionPerformed0(@NotNull ConflictPair pConflictPair)
    {
      IChangeDelta theirsDelta = theirsPaneWrapper.getFileDiff().getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.THEIRS));
      mergeDiff.acceptDelta(theirsDelta, EConflictSide.THEIRS);
      IChangeDelta yoursDelta = yoursPaneWrapper.getFileDiff().getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.YOURS));
      mergeDiff.discardChange(yoursDelta, EConflictSide.YOURS);
    }
  }

  private class UseLeftThenRightAction extends MergeChunkAction
  {

    public UseLeftThenRightAction()
    {
      super(iconLoader.getIcon(Constants.ACCEPT_LEFT_THEN_RIGHT_CONFLICT));
      putValue(SHORT_DESCRIPTION, "Apply the Left Change, then the Right Change");
    }

    @Override
    void actionPerformed0(@NotNull ConflictPair pConflictPair)
    {
      IChangeDelta yoursDelta = yoursPaneWrapper.getFileDiff().getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.YOURS));
      mergeDiff.acceptDelta(yoursDelta, EConflictSide.YOURS);
      IChangeDelta theirsDelta = theirsPaneWrapper.getFileDiff().getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.THEIRS));
      mergeDiff.acceptDelta(theirsDelta, EConflictSide.THEIRS);
    }
  }

  private class UseRightThenLeftAction extends MergeChunkAction
  {

    public UseRightThenLeftAction()
    {
      super(iconLoader.getIcon(Constants.ACCEPT_RIGHT_THEN_LEFT_CONFLICT));
      putValue(SHORT_DESCRIPTION, "Apply the Right Change, then the Left Change");
    }

    @Override
    void actionPerformed0(@NotNull ConflictPair pConflictPair)
    {
      IChangeDelta theirsDelta = theirsPaneWrapper.getFileDiff().getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.THEIRS));
      mergeDiff.acceptDelta(theirsDelta, EConflictSide.THEIRS);
      IChangeDelta yoursDelta = yoursPaneWrapper.getFileDiff().getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.YOURS));
      mergeDiff.acceptDelta(yoursDelta, EConflictSide.YOURS);
    }
  }


  /**
   * Basic class for all actions that resolve both sides of a conflict at the press of one button
   */
  public abstract class MergeChunkAction extends AbstractAction
  {

    IChangeDelta currentDelta = null;

    public MergeChunkAction(Icon icon)
    {
      super(null, icon);
    }

    public void setCurrentDelta(@Nullable IChangeDelta pChangeDelta)
    {
      currentDelta = pChangeDelta;
      setEnabled(currentDelta != null && currentDelta.getConflictType() != EConflictType.NONE);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (yoursPaneWrapper.isEditorFocusOwner())
      {
        ConflictPair conflictPair = mergeDiff.getConflictPair(currentDelta, yoursPaneWrapper.getFileDiff(), EConflictSide.YOURS);
        if (conflictPair != null)
        {
          actionPerformed0(conflictPair);
        }
      }
      else if (theirsPaneWrapper.isEditorFocusOwner())
      {
        ConflictPair conflictPair = mergeDiff.getConflictPair(currentDelta, theirsPaneWrapper.getFileDiff(), EConflictSide.THEIRS);
        if (conflictPair != null)
        {
          actionPerformed0(conflictPair);
        }
      }
    }

    /**
     * @param pConflictPair ConflictPair denoting the indices of the currently selected deltas that should be resolved
     */
    abstract void actionPerformed0(@NotNull ConflictPair pConflictPair);
  }
}
