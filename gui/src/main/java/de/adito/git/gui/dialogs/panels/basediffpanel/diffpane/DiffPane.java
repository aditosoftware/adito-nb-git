package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.gui.OnionColumnLayout;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import de.adito.git.gui.rxjava.ViewPortSizeObservable;
import de.adito.git.impl.observables.PropertyChangeObservable;
import de.adito.util.reactive.cache.ObservableCache;
import de.adito.util.reactive.cache.ObservableCacheDisposable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Contains a EditorPane, that is passed in the constructor, inside a ScrollPane. Around the EditorPane several LineNumPanels and ChoiceButtonPanels
 * can be added. These do scroll with the EditorPane, the LineNumPanel shows the lineNumbers of the text in the EditorPane, the ChoiceButtonPanels
 * offer a way to accept changes of a diff/merge
 *
 * @author m.kaspera, 11.01.2019
 */
public class DiffPane extends JPanel implements IDiscardable
{
  private final JScrollPane scrollPane = new JScrollPane();
  private final JEditorPane editorPane;
  private final List<IDiscardable> discardables = new ArrayList<>();
  private final ObservableCache observableCache = new ObservableCache();
  private final CompositeDisposable disposables = new CompositeDisposable();

  public DiffPane(JEditorPane pEditorPane)
  {
    editorPane = pEditorPane;
    disposables.add(new ObservableCacheDisposable(observableCache));
    setLayout(new OnionColumnLayout());
    scrollPane.setViewportView(editorPane);
    scrollPane.setBorder(null);
    add(scrollPane, OnionColumnLayout.CENTER);
  }

  @NotNull
  public JScrollPane getScrollPane()
  {
    return scrollPane;
  }

  /**
   * @param pTextChangeEventObservable Observable that fires a new DeltaTextChangeEvent if the text on any side of the Diff changes
   * @return LineNumberModel that keeps track of the y coordinates for each line in the editor of this diffPane
   */
  @NotNull
  public LineNumberModel createLineNumberModel(@NotNull Observable<IDeltaTextChangeEvent> pTextChangeEventObservable)
  {
    return new LineNumberModel(pTextChangeEventObservable, editorPane, _observeViewPortSize());
  }

  /**
   * Creates a new LineNumPanel and adds it to the layout
   *
   * @param pLineNumberModel        LineNumberModel that keeps track of the y coordinates of the lines
   * @param pLineChangeMarkingModel LineChangeMarkingsModel that keeps track of the y coordinates of the change markings
   * @param pLineOrientation        String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   *                                Defaults to BorderLayout.WEST if another String is passed
   */
  public void addLineNumPanel(@NotNull LineNumberModel pLineNumberModel, @NotNull LineChangeMarkingModel pLineChangeMarkingModel, @NotNull String pLineOrientation)
  {
    LineNumPanel lineNumPanel = new LineNumPanel(editorPane, pLineNumberModel, pLineChangeMarkingModel, getScrollPane().getViewport());
    discardables.add(lineNumPanel);
    add(lineNumPanel, pLineOrientation.equals(BorderLayout.EAST) ? OnionColumnLayout.RIGHT : OnionColumnLayout.LEFT);
  }

  /**
   * Creates a new ChoiceButtonPanel and adds it to the layout
   *
   * @param pModel              DiffPanelModel with the Observable list of fileChangeChunks
   * @param pLineNumberModel    LineNumberModel that keeps track of the y coordinates of the lines
   * @param pLeftMarkingsModel  ViewLineChangeMarkingsModel that keeps track of the position of the changeMarkings relative to the viewport
   * @param pRightMarkingsModel ViewLineChangeMarkingsModel that keeps track of the position of the changeMarkings relative to the viewport
   * @param pAcceptIcon         ImageIcon for the accept button
   * @param pDiscardIcon        ImageIcon for the discard button
   * @param pOrientation        String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   */
  public void addChoiceButtonPanel(@NotNull DiffPanelModel pModel, @NotNull LineNumberModel pLineNumberModel, @NotNull ViewLineChangeMarkingModel pLeftMarkingsModel,
                                   @NotNull ViewLineChangeMarkingModel pRightMarkingsModel, @Nullable ImageIcon pAcceptIcon, @Nullable ImageIcon pDiscardIcon,
                                   @NotNull String pOrientation)
  {
    ChoiceButtonPanel choiceButtonPanel = new ChoiceButtonPanel(pModel, editorPane, scrollPane.getViewport(), pLineNumberModel, pLeftMarkingsModel, pRightMarkingsModel,
                                                                pAcceptIcon, pDiscardIcon, pOrientation);
    discardables.add(choiceButtonPanel);
    add(choiceButtonPanel, pOrientation.equals(BorderLayout.EAST) ? OnionColumnLayout.RIGHT : OnionColumnLayout.LEFT);
  }

  @Override
  public void discard()
  {
    discardables.forEach(IDiscardable::discard);
    disposables.dispose();
    removeAll();
  }

  @NotNull
  private Observable<Dimension> _observeViewPortSize()
  {
    return observableCache.calculateParallel("viewPortSize", () -> Observable
        .combineLatest(Observable.create(new PropertyChangeObservable<Integer>(editorPane, "text-zoom"))
                           .startWithItem(Optional.empty()),
                       Observable.create(new ViewPortSizeObservable(scrollPane.getViewport())),
                       (pZoom, pViewportSize) -> pViewportSize)
        .throttleLatest(250, TimeUnit.MILLISECONDS, true));
  }

}
