package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.data.*;
import de.adito.git.gui.rxjava.EditorKitChangeObservable;
import de.adito.git.gui.rxjava.ViewPortSizeObservable;
import de.adito.git.gui.swing.IEditorUtils;
import de.adito.git.impl.util.BiNavigateAbleMap;
import de.adito.git.impl.util.DifferentialScrollBarCoupling;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import java.awt.Dimension;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author m.kaspera, 21.02.2019
 */
public interface IDiffPaneUtil
{

  /**
   * moves the caret to the position of the next changed chunk, as seen from the current position of the caret
   *
   * @param pTextComponent JTextComponent that is currently focused and whose content the IFileChangeChunks of the pModel describe
   * @param pChangeChunks  List of IFileChangeChunks
   * @param pChangeSide    which side of a IFileChangeChunk should be taken
   */
  static void moveCaretToNextChunk(JTextComponent pTextComponent, List<IFileChangeChunk> pChangeChunks,
                                   EChangeSide pChangeSide)
  {
    int caretLine = pTextComponent.getDocument().getDefaultRootElement().getElementIndex(pTextComponent.getCaret().getDot());
    int moveToElementStartLine = 0;
    for (IFileChangeChunk changeChunk : pChangeChunks)
    {
      if (changeChunk.getChangeType() != EChangeType.SAME && changeChunk.getStart(pChangeSide) > caretLine)
      {
        moveToElementStartLine = changeChunk.getStart(pChangeSide);
        break;
      }
    }
    pTextComponent.getCaret().setDot(pTextComponent.getDocument().getDefaultRootElement().getElement(moveToElementStartLine).getStartOffset());
    pTextComponent.requestFocus();
  }

  /**
   * moves the caret to the position of the previous changed chunk, as seen from the current position of the caret
   *
   * @param pTextComponent JTextComponent that is currently focused and whose content pChangeChunks describes
   * @param pChangeChunks  List of IFileChangeChunks
   * @param pChangeSide    which side of a IFileChangeChunk should be taken
   */
  static void moveCaretToPreviousChunk(JTextComponent pTextComponent, List<IFileChangeChunk> pChangeChunks, EChangeSide pChangeSide)
  {
    int caretLine = pTextComponent.getDocument().getDefaultRootElement().getElementIndex(pTextComponent.getCaret().getDot());
    int moveToElementStartLine = 0;
    for (int index = pChangeChunks.size() - 1; index >= 0; index--)
    {
      if (pChangeChunks.get(index).getChangeType() != EChangeType.SAME && pChangeChunks.get(index).getEnd(pChangeSide) <= caretLine)
      {
        moveToElementStartLine = pChangeChunks.get(index).getStart(pChangeSide);
        break;
      }
    }
    pTextComponent.getCaret().setDot(pTextComponent.getDocument().getDefaultRootElement().getElement(moveToElementStartLine).getStartOffset());
    pTextComponent.requestFocus();
  }

  /**
   * moves the caret to the position of the given changed chunk
   *
   * @param pTextComponent JTextComponent that is currently focused and whose content pChangeChunks describes
   * @param pMoveToChunk   The chunk that the caret should be moved to
   * @param pChangeSide    which side of a IFileChangeChunk should be taken
   */
  static void moveCaretToChunk(@NotNull JTextComponent pTextComponent, @NotNull IFileChangeChunk pMoveToChunk,
                               EChangeSide pChangeSide)
  {
    int moveToElementStartLine = pMoveToChunk.getStart(pChangeSide);
    pTextComponent.getCaret().setDot(pTextComponent.getDocument().getDefaultRootElement().getElement(moveToElementStartLine).getStartOffset());
    pTextComponent.requestFocus();
  }

  /**
   * synchronize two scrollPanes via the two editorPanes and the lines of the fileDiffs
   *
   * @param pScrollPaneOld     first scrollPane that should be synchronized
   * @param pScrollPaneCurrent second scrollPane that should be synchronized
   * @param pEditorPaneOld     editorPane that is contained in the first scrollPane
   * @param pEditorPaneCurrent editorPane that is contained in the second scrollPane
   * @param pFileDiffObs       Observable that has the current FileDiff
   */
  static DifferentialScrollBarCoupling synchronize(JScrollPane pScrollPaneOld, JScrollPane pScrollPaneCurrent, JEditorPane pEditorPaneOld,
                                                   JEditorPane pEditorPaneCurrent, Observable<Optional<IFileDiff>> pFileDiffObs)
  {
    Observable<Dimension> viewPort1SizeObs = Observable.create(new ViewPortSizeObservable(pScrollPaneOld.getViewport()));
    Observable<Dimension> viewPort2SizeObs = Observable.create(new ViewPortSizeObservable(pScrollPaneCurrent.getViewport()));
    Observable<Object> viewPortsObs = Observable.zip(viewPort1SizeObs, viewPort2SizeObs, (pSize1, pSize2) -> new Object());
    Observable<Object> editorKitsObs = Observable.combineLatest(Observable.create(new EditorKitChangeObservable(pEditorPaneOld)),
                                                                Observable.create(new EditorKitChangeObservable(pEditorPaneCurrent)), (o1, o2) -> o1);
    Observable<Object> editorViewChangeObs = Observable.merge(viewPortsObs, editorKitsObs);
    Observable<IFileChangesEvent> changesEventObservable = pFileDiffObs.switchMap(pFileDiffOpt -> pFileDiffOpt.map(pFDiff -> pFDiff.getFileChanges()
        .getChangeChunks()).orElseThrow());
    Observable<IFileChangesEvent> changeEventObs = Observable.combineLatest(editorViewChangeObs, changesEventObservable,
                                                                            (pObj, pChangeEvent) -> pChangeEvent);
    Function<IFileChangesEvent, BiNavigateAbleMap<Integer, Integer>> refreshFunction = pFileDiff ->
        getHeightMappings(pEditorPaneOld, pEditorPaneCurrent, pFileDiff);
    return DifferentialScrollBarCoupling.coupleScrollBars(pScrollPaneOld.getVerticalScrollBar(), pScrollPaneCurrent.getVerticalScrollBar(),
                                                          refreshFunction, changeEventObs);
  }

  /**
   * @param pEditorPaneOld     pEditorPane that contains the text of the IFileDiff with side OLD
   * @param pEditorPaneCurrent pEditorPane that contains the text of the IFileDiff with side NEW
   * @param pChangesEvent      IFileDiff with the information about the changes displayed in the editorPanes
   * @return List of Mappings of the y values of the IFileChangeChunks in the IFileDiff
   */
  static BiNavigateAbleMap<Integer, Integer> getHeightMappings(JEditorPane pEditorPaneOld, JEditorPane pEditorPaneCurrent,
                                                               IFileChangesEvent pChangesEvent)
  {
    BiNavigateAbleMap<Integer, Integer> heightMap = new BiNavigateAbleMap<>();
    // default entry: start is equal
    heightMap.put(0, 0);
    View oldEditorPaneView = pEditorPaneOld.getUI().getRootView(pEditorPaneOld);
    View currentEditorPaneView = pEditorPaneCurrent.getUI().getRootView(pEditorPaneCurrent);
    for (IFileChangeChunk changeChunk : pChangesEvent.getNewValue())
    {
      try
      {
        heightMap.put(IEditorUtils.getBoundsForChunk(changeChunk, EChangeSide.OLD, pEditorPaneOld, oldEditorPaneView),
                      IEditorUtils.getBoundsForChunk(changeChunk, EChangeSide.NEW, pEditorPaneCurrent, currentEditorPaneView));
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
}
