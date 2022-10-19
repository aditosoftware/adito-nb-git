package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.*;
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
  private LineNumbersColorModel leftForkPointLineNumColorModel;
  private LineNumbersColorModel rightForkPointLineNumColorModel;
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
    caretListener = new CaretMovedListener();
    yoursPaneWrapper.getEditorPane().addCaretListener(caretListener);
    theirsPaneWrapper.getEditorPane().addCaretListener(caretListener);
    forkPointPaneWrapper.getEditorPane().addCaretListener(caretListener);
  }

  public void finishLoading()
  {
    initHeightsObs.onNext(Optional.of(new Object()));
    yoursPaneWrapper.getScrollPane().getVerticalScrollBar().setValue(0);
    forkPointPaneWrapper.getScrollPane().getVerticalScrollBar().setValue(0);
    theirsPaneWrapper.getScrollPane().getVerticalScrollBar().setValue(0);
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
