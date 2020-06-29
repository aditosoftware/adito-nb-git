package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.IDiscardable;
import de.adito.git.gui.OnionColumnLayout;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import de.adito.git.gui.rxjava.ViewPortSizeObservable;
import de.adito.git.impl.observables.PropertyChangeObservable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
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


  private final Observable<Dimension> viewPortSizeObservable;
  private final JScrollPane scrollPane = new JScrollPane();
  private final JEditorPane editorPane;
  private final List<IDiscardable> discardables = new ArrayList<>();
  private final CompositeDisposable disposables = new CompositeDisposable();

  public DiffPane(JEditorPane pEditorPane)
  {
    editorPane = pEditorPane;
    Observable<Optional<Integer>> zoomObservable = Observable.create(new PropertyChangeObservable<Integer>(editorPane, "text-zoom"))
        .startWithItem(Optional.empty())
        .replay(1)
        .autoConnect(0, disposables::add);
    Observable<Dimension> basicViewPortSizeObservable = Observable.create(new ViewPortSizeObservable(scrollPane.getViewport()));
    viewPortSizeObservable = Observable.combineLatest(zoomObservable, basicViewPortSizeObservable, (pZoom, pViewportSize) -> pViewportSize)
        .replay(1)
        .autoConnect(0, disposables::add)
        .throttleLatest(250, TimeUnit.MILLISECONDS);
    setLayout(new OnionColumnLayout());
    scrollPane.setViewportView(editorPane);
    scrollPane.setBorder(null);
    add(scrollPane, OnionColumnLayout.CENTER);
  }

  public JScrollPane getScrollPane()
  {
    return scrollPane;
  }

  /**
   * @param pModel             DiffPanelModel with the Observable list of fileChangeChunks
   * @param pInitHeightCalcObs Observable that fires once when the heights are ready to be calculated
   * @param pLineOrientation   String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   *                           Defaults to BorderLayout.WEST if another String is passed
   * @param pModelNumber       number of the LineNumbersColorModel used in the LineNumPanel, also returned by this method
   * @return LineNumbersColorModel describing the
   */
  public LineNumbersColorModel addLineNumPanel(DiffPanelModel pModel, Observable<Optional<Object>> pInitHeightCalcObs, String pLineOrientation, int pModelNumber)
  {
    LineNumbersColorModel lineNumbersColorModel = createLineNumberColorModel(pModel, pInitHeightCalcObs, pModelNumber);
    addLineNumPanel(lineNumbersColorModel, pModel, pLineOrientation);
    return lineNumbersColorModel;
  }

  /**
   * Only creates the LineNumbersColorModel, it is not added to the Layout.
   *
   * @param pModel             DiffPanelModel with the Observable list of fileChangeChunks
   * @param pInitHeightCalcObs Observable that fires once when the heights are ready to be calculated
   * @param pModelNumber       number of the LineNumbersColorModel used in the LineNumPanel, also returned by this method
   * @return the new LineNumbersColorModel
   * @see #addLineNumPanel(DiffPanelModel, Observable, String, int)
   */
  public LineNumbersColorModel createLineNumberColorModel(DiffPanelModel pModel, Observable<Optional<Object>> pInitHeightCalcObs, int pModelNumber)
  {
    return new LineNumbersColorModel(pModel, editorPane, scrollPane.getViewport(),
                                     Observable.combineLatest(pInitHeightCalcObs, viewPortSizeObservable, (pObj, pViewPortSize) -> pViewPortSize),
                                     pModelNumber);
  }

  /**
   * Adds the LineNumbersColorModel to the Layout. Therefore a LineNumPanel is created.
   *
   * @param pLineNumbersColorModel the LineNumbersColorModel, which should be added
   * @param pModel                 DiffPanelModel with the Observable list of fileChangeChunks
   * @param pLineOrientation       String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   *                               Defaults to BorderLayout.WEST if another String is passed
   */
  public void addLineNumPanel(LineNumbersColorModel pLineNumbersColorModel, DiffPanelModel pModel, String pLineOrientation)
  {
    LineNumPanel lineNumPanel = new LineNumPanel(pModel, editorPane, scrollPane.getViewport(), viewPortSizeObservable, pLineNumbersColorModel);
    discardables.add(lineNumPanel);
    add(lineNumPanel, pLineOrientation.equals(BorderLayout.EAST) ? OnionColumnLayout.RIGHT : OnionColumnLayout.LEFT);
  }

  /**
   * @param pModel                  DiffPanelModel with the Observable list of fileChangeChunks
   * @param pAcceptIcon             ImageIcon for the accept button
   * @param pDiscardIcon            ImageIcon for the discard button
   * @param pLineNumbersColorModels Array of size 2 with LineNumPanels, index 0 is the LineNumPanel to the left of this ChoiceButtonPane,
   *                                1 to the right
   * @param pOrientation            String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   */
  public void addChoiceButtonPanel(@NotNull DiffPanelModel pModel, @Nullable ImageIcon pAcceptIcon, @Nullable ImageIcon pDiscardIcon,
                                   LineNumbersColorModel[] pLineNumbersColorModels, @NotNull String pOrientation)
  {
    ChoiceButtonPanel choiceButtonPanel = new ChoiceButtonPanel(pModel, editorPane, scrollPane.getViewport(), viewPortSizeObservable,
                                                                pAcceptIcon, pDiscardIcon, pLineNumbersColorModels, pOrientation);
    discardables.add(choiceButtonPanel);
    add(choiceButtonPanel, pOrientation.equals(BorderLayout.EAST) ? OnionColumnLayout.RIGHT : OnionColumnLayout.LEFT);
  }

  @Override
  public void discard()
  {
    discardables.forEach(IDiscardable::discard);
    disposables.dispose();
  }

}
