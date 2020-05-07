package de.adito.git.gui.dialogs.panels.basediffpanel;

import com.google.common.collect.Collections2;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.*;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.IPaneWrapper;
import de.adito.git.gui.rxjava.EditorKitChangeObservable;
import de.adito.git.gui.rxjava.ViewPortSizeObservable;
import de.adito.git.gui.swing.IEditorUtils;
import de.adito.git.gui.swing.SynchronizedBoundedRangeModel;
import de.adito.git.impl.util.BiNavigateAbleMap;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author m.kaspera, 21.02.2019
 */
public interface IDiffPaneUtil
{

  /**
   * retrieves the position of the next changed chunk, as seen from the current position of the caret
   *
   * @param pTextComponent JTextComponent that is currently focused and whose content the IFileChangeChunks of the pModel describe
   * @param pChangeDeltas  List of IChangeDelta
   * @param pChangeSide    which side of a IFileChangeChunk should be taken
   */
  @Nullable
  static IChangeDelta getNextDelta(JTextComponent pTextComponent, List<IChangeDelta> pChangeDeltas, EChangeSide pChangeSide)
  {
    int caretLine = pTextComponent.getDocument().getDefaultRootElement().getElementIndex(pTextComponent.getCaret().getDot());
    IChangeDelta nextChunk = null;
    for (IChangeDelta changeChunk : pChangeDeltas)
    {
      nextChunk = changeChunk;
      if (changeChunk.getChangeStatus() == EChangeStatus.PENDING && changeChunk.getStartLine(pChangeSide) > caretLine)
      {
        break;
      }
    }
    return nextChunk;
  }

  /**
   * retrieves the previous changed chunk, as seen from the current position of the caret
   *
   * @param pTextComponent JTextComponent that is currently focused and whose content pChangeDeltas describes
   * @param pChangeDeltas  List of IChangeDelta
   * @param pChangeSide    which side of a IFileChangeChunk should be taken
   */
  @Nullable
  static IChangeDelta getPreviousDelta(JTextComponent pTextComponent, List<IChangeDelta> pChangeDeltas, EChangeSide pChangeSide)
  {
    int caretLine = pTextComponent.getDocument().getDefaultRootElement().getElementIndex(pTextComponent.getCaret().getDot());
    IChangeDelta previousChunk = null;
    for (int index = pChangeDeltas.size() - 1; index >= 0; index--)
    {
      if (pChangeDeltas.get(index).getChangeStatus() == EChangeStatus.PENDING && pChangeDeltas.get(index).getEndLine(pChangeSide) <= caretLine)
      {
        previousChunk = pChangeDeltas.get(index);
        if (pChangeDeltas.get(index).getStartLine(pChangeSide) != pChangeDeltas.get(index).getEndLine(pChangeSide)
            || caretLine > pChangeDeltas.get(index).getEndLine(pChangeSide))
          break;
      }
    }
    return previousChunk;
  }

  /**
   * moves the caret to the position of the given changed chunk
   *
   * @param pTextComponent JTextComponent that is currently focused and whose content pChangeDeltas describes
   * @param pMoveToDelta   The delta that the caret should be moved to. If null, the caret is not moved at all
   * @param pChangeSide    which side of a IFileChangeChunk should be taken
   */
  static void moveCaretToDelta(@NotNull JTextComponent pTextComponent, @Nullable IChangeDelta pMoveToDelta, EChangeSide pChangeSide)
  {
    if (pMoveToDelta == null)
      return;
    int moveToElementStartLine = pMoveToDelta.getStartLine(pChangeSide);
    pTextComponent.getCaret().setDot(pTextComponent.getDocument().getDefaultRootElement().getElement(moveToElementStartLine).getStartOffset());
    pTextComponent.requestFocus();
  }

  /**
   * synchronize two scrollPanes via the two editorPanes and the lines of the fileDiffs
   *
   * @param pOldPane     first IPaneWrapper whose scrollPane should be synchronized
   * @param pCurrentPane second IPaneWrapper whose scrollPane should be synchronized
   * @param pFileDiffObs Observable that has the current FileDiff
   */
  static ScrollBarCoupling synchronize(@NotNull IPaneWrapper pOldPane, @Nullable String pModel1Key, @NotNull IPaneWrapper pCurrentPane, @Nullable String pModel2Key,
                                       Observable<Optional<IFileDiff>> pFileDiffObs)
  {
    CompositeDisposable disposables = new CompositeDisposable();
    Observable<Dimension> viewPort1SizeObs = Observable.create(new ViewPortSizeObservable(pOldPane.getScrollPane().getViewport()));
    Observable<Dimension> viewPort2SizeObs = Observable.create(new ViewPortSizeObservable(pCurrentPane.getScrollPane().getViewport()));
    Observable<Object> viewPortsObs = Observable.zip(viewPort1SizeObs, viewPort2SizeObs, (pSize1, pSize2) -> new Object());
    Observable<Object> editorKitsObs = Observable.combineLatest(Observable.create(new EditorKitChangeObservable(pOldPane.getEditorPane())),
                                                                Observable.create(new EditorKitChangeObservable(pCurrentPane.getEditorPane())), (o1, o2) -> o1);
    Observable<Object> editorViewChangeObs = Observable.merge(viewPortsObs, editorKitsObs);
    Observable<Optional<List<IChangeDelta>>> changesEventObservable = pFileDiffObs
        .switchMap(pFileDiffOpt -> pFileDiffOpt.map(pFDiff -> pFDiff.getDiffTextChangeObservable()
            .map(pTextChangeEvent -> Optional.of(pFDiff.getChangeDeltas()))).orElse(Observable.just(Optional.empty())));
    Observable<Optional<List<IChangeDelta>>> changeEventObs = Observable.combineLatest(editorViewChangeObs, changesEventObservable,
                                                                                       (pObj, pChangeEvent) -> pChangeEvent)
        .replay(1)
        .autoConnect(0, disposables::add);
    Function<List<IChangeDelta>, BiNavigateAbleMap<Integer, Integer>> refreshFunction = pFileDiff ->
        getHeightMappings(pOldPane.getEditorPane(), pCurrentPane.getEditorPane(), pFileDiff);
    _syncPanes(pOldPane.getScrollPane(), pCurrentPane.getScrollPane(), refreshFunction, changeEventObs, false);
    _syncPanes(pCurrentPane.getScrollPane(), pOldPane.getScrollPane(), refreshFunction, changeEventObs, true);
    return new ScrollBarCoupling((SynchronizedBoundedRangeModel) pOldPane.getScrollPane().getVerticalScrollBar().getModel(), pModel1Key,
                                 (SynchronizedBoundedRangeModel) pCurrentPane.getScrollPane().getVerticalScrollBar().getModel(), pModel2Key, disposables);
  }

  /**
   * Initializes and sets a SynchronizedBoundedRangeModel to pScrollPane. That model is already coupled with pSecondScrollPane.
   * If the model of pScrollPane is already a SynchronizedBoundedRangeModel, adds the seconds ScrollBar to that model
   *
   * @param pScrollPane        JScrollPane whose model should be set or coupled with pSecondScrollPane
   * @param pSecondScrollPane  JScrollPane that should be coupled to pScrollPane
   * @param pRefreshFunction   Function that refreshes the heightMappings for the SynchronizedBoundedRangeModel
   * @param pChangeEventObs    Observable that offers the latest version of the IFileChangesEvent describing the changes displayed in the scrollPanes
   * @param pUseInverseMapping Determines if the scrollPane uses the inverse of the height mappings. When combining two scrollPanes, one should have true here, the other
   *                           false
   */
  private static void _syncPanes(@NotNull JScrollPane pScrollPane, @NotNull JScrollPane pSecondScrollPane,
                                 @NotNull Function<List<IChangeDelta>, BiNavigateAbleMap<Integer, Integer>> pRefreshFunction,
                                 @NotNull Observable<Optional<List<IChangeDelta>>> pChangeEventObs, boolean pUseInverseMapping)
  {
    if (pScrollPane.getVerticalScrollBar().getModel() instanceof SynchronizedBoundedRangeModel)
    {
      ((SynchronizedBoundedRangeModel) pScrollPane.getVerticalScrollBar().getModel())
          .addCoupledScrollbar(pSecondScrollPane.getVerticalScrollBar(), pRefreshFunction, pChangeEventObs, pUseInverseMapping);
    }
    else
    {
      SynchronizedBoundedRangeModel scrollBarOldModel = new SynchronizedBoundedRangeModel(pSecondScrollPane.getVerticalScrollBar(), pRefreshFunction,
                                                                                          pChangeEventObs, pUseInverseMapping);
      pScrollPane.getVerticalScrollBar().setModel(scrollBarOldModel);
    }
  }

  /**
   * @param pEditorPaneOld     pEditorPane that contains the text of the IFileDiff with side OLD
   * @param pEditorPaneCurrent pEditorPane that contains the text of the IFileDiff with side NEW
   * @param pChangeDeltas      IFileDiff with the information about the changes displayed in the editorPanes
   * @return List of Mappings of the y values of the IFileChangeChunks in the IFileDiff
   */
  static BiNavigateAbleMap<Integer, Integer> getHeightMappings(JEditorPane pEditorPaneOld, JEditorPane pEditorPaneCurrent,
                                                               List<IChangeDelta> pChangeDeltas)
  {
    BiNavigateAbleMap<Integer, Integer> heightMap = new BiNavigateAbleMap<>();
    // default entry: start is equal
    heightMap.put(0, 0);
    View oldEditorPaneView = pEditorPaneOld.getUI().getRootView(pEditorPaneOld);
    View currentEditorPaneView = pEditorPaneCurrent.getUI().getRootView(pEditorPaneCurrent);
    for (IChangeDelta changeChunk : pChangeDeltas)
    {
      try
      {
        heightMap.put(IEditorUtils.getBoundsForChunk(changeChunk, EChangeSide.OLD, pEditorPaneOld, oldEditorPaneView),
                      IEditorUtils.getBoundsForChunk(changeChunk, EChangeSide.NEW, pEditorPaneCurrent, currentEditorPaneView));
        heightMap.put(IEditorUtils.getEndBoundsForChunk(changeChunk, EChangeSide.OLD, pEditorPaneOld, oldEditorPaneView),
                      IEditorUtils.getEndBoundsForChunk(changeChunk, EChangeSide.NEW, pEditorPaneCurrent, currentEditorPaneView));
      }
      catch (IllegalArgumentException iAE)
      {
        // do nothing, key
      }
      catch (BadLocationException pE)
      {
        throw new RuntimeException(pE);
      }
    }
    return heightMap;
  }

  /**
   * Connect all passed boundedModels such that their scrolling is coupled (until one hits the maximum, at which point only the others move until the value drops
   * below the value of the first model again).
   * The lists can be empty
   *
   * @param pUpwardsModels BoundedModels in their normal form
   */
  static void bridge(@NotNull List<BoundedRangeModel> pUpwardsModels)
  {
    for (BoundedRangeModel model : pUpwardsModels)
    {
      model.addChangeListener(pE -> Collections2
          .filter(pUpwardsModels, pModel -> pModel != null && !pModel.equals(model))
          .forEach(pModel -> pModel.setValue(model.getValue())));
    }
  }

  /**
   * Class that may discard the two saved SynchronizedBoundedRangeModel, but nothing else
   */
  class ScrollBarCoupling implements IDiscardable
  {
    private final HashMap<String, SynchronizedBoundedRangeModel> modelMap = new HashMap<>();
    private final CompositeDisposable disposables;

    ScrollBarCoupling(@NotNull SynchronizedBoundedRangeModel pModel1, @Nullable String pModel1Key, @NotNull SynchronizedBoundedRangeModel pModel2,
                      @Nullable String pModel2Key, CompositeDisposable pDisposables)
    {
      modelMap.put(pModel1Key == null ? "model1" : pModel1Key, pModel1);
      modelMap.put(pModel2Key == null ? "model2" : pModel2Key, pModel2);
      disposables = pDisposables;
    }

    /**
     * tries to retrieve a model for the given key. Default values for the key would be "model1" and "model2"
     *
     * @param pModelKey key for the model
     * @return SynchronizedBoundedRangeModel registered with the given key, or null if none exists for that key
     */
    @Nullable
    public SynchronizedBoundedRangeModel getModel(@NotNull String pModelKey)
    {
      return modelMap.get(pModelKey);
    }

    @Override
    public void discard()
    {
      modelMap.forEach((key, value) -> value.discard());
      disposables.clear();
    }
  }
}
