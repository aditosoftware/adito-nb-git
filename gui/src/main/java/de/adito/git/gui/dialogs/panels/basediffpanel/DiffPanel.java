package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.*;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.LineNumbersColorModel;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.DiffPaneWrapper;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.swing.EditorUtils;
import de.adito.git.impl.data.FileChangesEventImpl;
import de.adito.git.impl.util.BiNavigateAbleMap;
import de.adito.git.impl.util.DifferentialScrollBarCoupling;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.EditorKit;
import javax.swing.text.View;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.util.Collections;
import java.util.Optional;

/**
 * Class to handle the basic layout of the two panels that display the differences between two files
 *
 * @author m.kaspera 12.11.2018
 */
public class DiffPanel extends JPanel implements IDiscardable
{

  private final DiffPaneWrapper currentVersionDiffPane;
  private final DiffPaneWrapper oldVersionDiffPane;
  private DifferentialScrollBarCoupling differentialScrollBarCoupling = null;

  /**
   * @param pIconLoader          IIconLoader for loading Icons
   * @param pFileDiffObs         Observable of the IFileDiff
   * @param pAcceptIcon          ImageIcon that is used for the "accept these changes" button
   * @param pEditorKitObservable EditorKitObservable that holds the editorKit for the current IFileDiff
   */
  public DiffPanel(IIconLoader pIconLoader, @NotNull Observable<Optional<IFileDiff>> pFileDiffObs,
                   @Nullable ImageIcon pAcceptIcon, Observable<EditorKit> pEditorKitObservable)
  {
    Observable<IFileChangesEvent> changesEventObservable = pFileDiffObs.switchMap(pFileDiff -> pFileDiff
        .map(pDiff -> pDiff.getFileChanges().getChangeChunks())
        .orElse(Observable.just(new FileChangesEventImpl(true, Collections.emptyList(), null))));
    LineNumbersColorModel[] lineNumbersColorModels = new LineNumbersColorModel[2];
    DiffPanelModel currentDiffPanelModel = new DiffPanelModel(changesEventObservable, EChangeSide.NEW);
    currentVersionDiffPane = new DiffPaneWrapper(currentDiffPanelModel, pEditorKitObservable);
    // current panel is to the right, so index 1
    LineNumbersColorModel rightLineColorModel = currentVersionDiffPane.getPane().addLineNumPanel(currentDiffPanelModel, BorderLayout.WEST, 1);
    lineNumbersColorModels[1] = rightLineColorModel;
    JScrollPane currentVersionScrollPane = currentVersionDiffPane.getScrollPane();
    currentVersionScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    DiffPanelModel oldDiffPanelModel = new DiffPanelModel(changesEventObservable, EChangeSide.OLD)
        .setDoOnAccept(pChangeChunk -> pFileDiffObs.blockingFirst().ifPresent(pFileDiff -> pFileDiff.getFileChanges().resetChanges(pChangeChunk)));
    oldVersionDiffPane = new DiffPaneWrapper(oldDiffPanelModel, pEditorKitObservable);
    // old version is to the left, so index 0
    LineNumbersColorModel leftLineColorsModel = oldVersionDiffPane.getPane().addLineNumPanel(oldDiffPanelModel, BorderLayout.EAST, 0);
    lineNumbersColorModels[0] = leftLineColorsModel;
    oldVersionDiffPane.getPane().addChoiceButtonPanel(oldDiffPanelModel, pAcceptIcon, null, lineNumbersColorModels,
                                                      BorderLayout.EAST);
    JScrollPane oldVersionScrollPane = oldVersionDiffPane.getScrollPane();
    oldVersionScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    oldVersionScrollPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    _synchronize(oldVersionScrollPane, currentVersionScrollPane, oldVersionDiffPane.getEditorPane(), currentVersionDiffPane.getEditorPane(),
                 pFileDiffObs);
    setLayout(new BorderLayout());
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, oldVersionDiffPane.getPane(), currentVersionDiffPane.getPane());
    mainSplitPane.setResizeWeight(0.5);
    setBorder(new JScrollPane().getBorder());
    add(mainSplitPane, BorderLayout.CENTER);
    add(_initToolBar(pIconLoader), BorderLayout.NORTH);
  }

  @Override
  public void discard()
  {
    currentVersionDiffPane.discard();
    oldVersionDiffPane.discard();
    differentialScrollBarCoupling.discard();
  }

  private JToolBar _initToolBar(IIconLoader pIconLoader)
  {
    JButton nextButton = new JButton(pIconLoader.getIcon(Constants.NEXT_OCCURRENCE));
    nextButton.setFocusable(false);
    nextButton.addActionListener(e -> {
      if (currentVersionDiffPane.isEditorFocusOwner())
        currentVersionDiffPane.moveCaretToNextChunk();
      else
        oldVersionDiffPane.moveCaretToNextChunk();
    });
    JButton previousButton = new JButton(pIconLoader.getIcon(Constants.PREVIOUS_OCCURRENCE));
    previousButton.setFocusable(false);
    previousButton.addActionListener(e -> {
      if (currentVersionDiffPane.isEditorFocusOwner())
        currentVersionDiffPane.moveCaretToPreviousChunk();
      else
        oldVersionDiffPane.moveCaretToPreviousChunk();
    });
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(nextButton);
    toolBar.add(previousButton);
    return toolBar;
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
  private void _synchronize(JScrollPane pScrollPaneOld, JScrollPane pScrollPaneCurrent, JEditorPane pEditorPaneOld, JEditorPane pEditorPaneCurrent,
                            Observable<Optional<IFileDiff>> pFileDiffObs)
  {
    Observable<Dimension> viewPort1SizeObs = Observable.create(new _ViewPortSizeObservable(pScrollPaneOld.getViewport()));
    Observable<Dimension> viewPort2SizeObs = Observable.create(new _ViewPortSizeObservable(pScrollPaneCurrent.getViewport()));
    Observable<Dimension> viewPortsObs = Observable.zip(viewPort1SizeObs, viewPort2SizeObs, (pSize1, pSize2) -> pSize1);
    Observable<BiNavigateAbleMap<Integer, Integer>> mapObservable =
        Observable.combineLatest(viewPortsObs, pFileDiffObs, (pViewportSize, pFileDiffsOpt) -> {
          BiNavigateAbleMap<Integer, Integer> heightMap = new BiNavigateAbleMap<>();
          if (pFileDiffsOpt.isPresent())
          {
            // default entry: start is equal
            heightMap.put(0, 0);
            View oldEditorPaneView = pEditorPaneOld.getUI().getRootView(pEditorPaneOld);
            View currentEditorPaneView = pEditorPaneCurrent.getUI().getRootView(pEditorPaneCurrent);
            for (IFileChangeChunk changeChunk : pFileDiffsOpt.get().getFileChanges().getChangeChunks().blockingFirst().getNewValue())
            {
              heightMap.put(EditorUtils.getBoundsForChunk(changeChunk, EChangeSide.OLD, pEditorPaneOld, oldEditorPaneView),
                            EditorUtils.getBoundsForChunk(changeChunk, EChangeSide.NEW, pEditorPaneCurrent, currentEditorPaneView));
            }
          }
          return heightMap;
        });
    differentialScrollBarCoupling = DifferentialScrollBarCoupling.coupleScrollBars(pScrollPaneOld.getVerticalScrollBar(),
                                                                                   pScrollPaneCurrent.getVerticalScrollBar(), mapObservable);
  }

  /**
   * Observable that fires each time the size of the viewPort window changes
   */
  private static class _ViewPortSizeObservable extends AbstractListenerObservable<ChangeListener, JViewport, Dimension>
  {

    private Dimension cachedViewportSize = new Dimension(0, 0);

    _ViewPortSizeObservable(@NotNull JViewport pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected ChangeListener registerListener(@NotNull JViewport pJViewport, @NotNull IFireable<Dimension> pIFireable)
    {
      ChangeListener changeListener = e -> {
        if (!pJViewport.getViewSize().equals(cachedViewportSize))
        {
          cachedViewportSize = pJViewport.getViewSize();
          pIFireable.fireValueChanged(pJViewport.getViewSize());
        }
      };
      pJViewport.addChangeListener(changeListener);
      return changeListener;
    }

    @Override
    protected void removeListener(@NotNull JViewport pJViewport, @NotNull ChangeListener pChangeListener)
    {
      pJViewport.removeChangeListener(pChangeListener);
    }
  }

}