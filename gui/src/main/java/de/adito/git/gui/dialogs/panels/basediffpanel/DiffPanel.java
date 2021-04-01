package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.api.data.diff.IFileDiff;
import de.adito.git.gui.Constants;
import de.adito.git.gui.LeftSideVSBScrollPaneLayout;
import de.adito.git.gui.dialogs.TextComparatorInfoPanel;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.LineNumbersColorModel;
import de.adito.git.gui.dialogs.panels.basediffpanel.textpanes.DiffPaneWrapper;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.swing.SwingUtil;
import de.adito.git.impl.data.diff.DeltaTextChangeEventImpl;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.BorderLayout;
import java.util.List;
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
  private IDiffPaneUtil.ScrollBarCoupling differentialScrollBarCoupling;
  private final CompositeDisposable disposables = new CompositeDisposable();
  private final Subject<Optional<Object>> initHeightsObs;

  /**
   * @param pIconLoader          IIconLoader for loading Icons
   * @param pFileDiffObs         Observable of the IFileDiff
   * @param pAcceptIcon          ImageIcon that is used for the "accept these changes" button
   * @param pEditorKitObservable EditorKitObservable that holds the editorKit for the current IFileDiff
   */
  public DiffPanel(@NotNull IIconLoader pIconLoader, @NotNull Observable<Optional<IFileDiff>> pFileDiffObs,
                   @Nullable ImageIcon pAcceptIcon, @NotNull Observable<Optional<EditorKit>> pEditorKitObservable, @Nullable String pLeftHeader,
                   @Nullable String pRightHeader)
  {
    Observable<IDeltaTextChangeEvent> changesEventObservable = pFileDiffObs
        .switchMap(pFileDiff -> pFileDiff
            .map(IFileDiff::getDiffTextChangeObservable)
            .orElse(Observable.just(new DeltaTextChangeEventImpl(0, 0, "", null))))
        .replay()
        .autoConnect(2, disposables::add);
    LineNumbersColorModel[] lineNumbersColorModels = new LineNumbersColorModel[2];
    DiffPanelModel currentDiffPanelModel = new DiffPanelModel(changesEventObservable, EChangeSide.NEW);
    currentVersionDiffPane = new DiffPaneWrapper(currentDiffPanelModel, pLeftHeader, SwingConstants.RIGHT, pEditorKitObservable);
    DiffPanelModel oldDiffPanelModel = new DiffPanelModel(changesEventObservable, EChangeSide.OLD)
        .setDoOnAccept(pChangeDelta -> pFileDiffObs.blockingFirst().ifPresent(pFileDiff -> pFileDiff.revertDelta(pChangeDelta, true)));
    oldVersionDiffPane = new DiffPaneWrapper(oldDiffPanelModel, pRightHeader, SwingConstants.LEFT, pEditorKitObservable);
    initHeightsObs = BehaviorSubject.create();
    initHeightsObs.onNext(Optional.empty());
    // current panel is to the right, so index 1
    LineNumbersColorModel rightLineColorModel = currentVersionDiffPane.getPaneContainer().addLineNumPanel(currentDiffPanelModel,
                                                                                                          initHeightsObs,
                                                                                                          BorderLayout.WEST, 1);
    lineNumbersColorModels[1] = rightLineColorModel;
    JScrollPane currentVersionScrollPane = currentVersionDiffPane.getScrollPane();
    currentVersionScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);

    // Neccessary for the left ChoiceButtonPanel, but should not be added to the Layout
    LineNumbersColorModel temp = oldVersionDiffPane.getPaneContainer().createLineNumberColorModel(oldDiffPanelModel, initHeightsObs, -1);

    // old version is to the left, so index 0
    LineNumbersColorModel leftLineColorsModel = oldVersionDiffPane.getPaneContainer().createLineNumberColorModel(oldDiffPanelModel,
                                                                                                                 initHeightsObs, 0);
    lineNumbersColorModels[0] = leftLineColorsModel;

    oldVersionDiffPane.getPaneContainer().addChoiceButtonPanel(oldDiffPanelModel, pAcceptIcon, null, new LineNumbersColorModel[]{temp, leftLineColorsModel},
                                                               BorderLayout.EAST);
    oldVersionDiffPane.getPaneContainer().addLineNumPanel(leftLineColorsModel, oldDiffPanelModel, BorderLayout.EAST);
    oldVersionDiffPane.getPaneContainer().addChoiceButtonPanel(oldDiffPanelModel, null, null, lineNumbersColorModels, BorderLayout.EAST);

    JScrollPane oldVersionScrollPane = oldVersionDiffPane.getScrollPane();
    oldVersionScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.SCROLL_SPEED_INCREMENT);
    oldVersionScrollPane.setLayout(new LeftSideVSBScrollPaneLayout());
    setLayout(new BorderLayout());
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, oldVersionDiffPane.getPaneContainer(), currentVersionDiffPane.getPaneContainer());
    mainSplitPane.setResizeWeight(0.5);
    setBorder(new JScrollPane().getBorder());
    add(mainSplitPane, BorderLayout.CENTER);
    JPanel optionsPanel = _getOptionsPanel(pIconLoader);
    add(optionsPanel, BorderLayout.NORTH);
    // couple horizontal scrollbars
    IDiffPaneUtil.bridge(List.of(currentVersionScrollPane.getHorizontalScrollBar().getModel(), oldVersionScrollPane.getHorizontalScrollBar().getModel()));
    SwingUtil.invokeASAP(() -> currentVersionScrollPane.getHorizontalScrollBar().setValue(0));
    SwingUtilities.invokeLater(() -> differentialScrollBarCoupling = IDiffPaneUtil.synchronize(oldVersionDiffPane, null, currentVersionDiffPane, null,
                                                                                               initHeightsObs, pFileDiffObs));
  }

  public void finishLoading()
  {
    initHeightsObs.onNext(Optional.of(new Object()));
    currentVersionDiffPane.getScrollPane().getVerticalScrollBar().setValue(0);
    oldVersionDiffPane.getScrollPane().getVerticalScrollBar().setValue(0);
  }

  /**
   * creates the Panel with the Toolbar and Options
   *
   * @param pIconLoader IconLoader for the Toolbar icons
   * @return JPanel with the aforementioned elements
   */
  @NotNull
  private JPanel _getOptionsPanel(IIconLoader pIconLoader)
  {
    JPanel optionsPanel = new JPanel(new BorderLayout());
    JToolBar toolBar = _initToolBar(pIconLoader);
    optionsPanel.add(toolBar, BorderLayout.WEST);
    optionsPanel.add(new TextComparatorInfoPanel(), BorderLayout.EAST);
    optionsPanel.setBorder(toolBar.getBorder());
    toolBar.setBorder(null);
    return optionsPanel;
  }

  @Override
  public void discard()
  {
    currentVersionDiffPane.discard();
    oldVersionDiffPane.discard();
    differentialScrollBarCoupling.discard();
    disposables.dispose();
  }

  private JToolBar _initToolBar(IIconLoader pIconLoader)
  {
    JButton nextButton = new JButton(pIconLoader.getIcon(Constants.NEXT_OCCURRENCE));
    nextButton.setFocusable(false);
    nextButton.addActionListener(e -> {
      if (currentVersionDiffPane.isEditorFocusOwner())
        currentVersionDiffPane.moveCaretToNextChunk(oldVersionDiffPane.getEditorPane());
      else
        oldVersionDiffPane.moveCaretToNextChunk(currentVersionDiffPane.getEditorPane());
    });
    JButton previousButton = new JButton(pIconLoader.getIcon(Constants.PREVIOUS_OCCURRENCE));
    previousButton.setFocusable(false);
    previousButton.addActionListener(e -> {
      if (currentVersionDiffPane.isEditorFocusOwner())
        currentVersionDiffPane.moveCaretToPreviousChunk(oldVersionDiffPane.getEditorPane());
      else
        oldVersionDiffPane.moveCaretToPreviousChunk(currentVersionDiffPane.getEditorPane());
    });
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(nextButton);
    toolBar.add(previousButton);
    return toolBar;
  }
}