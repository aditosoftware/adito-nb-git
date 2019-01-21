package de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.DiffPane;

import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.OnionColumnLayout;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.DiffPanelModel;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains a EditorPane, that is passed in the constructor, inside a ScrollPane. Around the EditorPane several LineNumPanels and ChoiceButtonPanels
 * can be added. These do scroll with the EditorPane, the LineNumPanel shows the lineNumbers of the text in the EditorPane, the ChoiceButtonPanels
 * offer a way to accept changes of a diff/merge
 *
 * @author m.kaspera, 11.01.2019
 */
public class DiffPane extends JPanel implements IDiscardable
{


  private final Observable<Rectangle> viewPortObservable;
  private final JScrollPane scrollPane = new JScrollPane();
  private final JEditorPane editorPane;
  private final List<IDiscardable> discardables = new ArrayList<>();

  public DiffPane(JEditorPane pEditorPane)
  {
    editorPane = pEditorPane;
    viewPortObservable = Observable.create(new ViewPortObservable(scrollPane.getViewport())).distinctUntilChanged();
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
   * @param pModel           DiffPanelModel with the Observable list of fileChangeChunks
   * @param pLineOrientation String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   *                         Defaults to BorderLayout.EAST if another String is passed
   */
  public LineNumPanel addLineNumPanel(DiffPanelModel pModel, String pLineOrientation)
  {
    LineNumPanel lineNumPanel = new LineNumPanel(pModel, editorPane, viewPortObservable);
    discardables.add(lineNumPanel);
    add(lineNumPanel, pLineOrientation.equals(BorderLayout.EAST) ? OnionColumnLayout.RIGHT : OnionColumnLayout.LEFT);
    return lineNumPanel;
  }

  /**
   * @param pModel         DiffPanelModel with the Observable list of fileChangeChunks
   * @param pAcceptIcon    ImageIcon for the accept button
   * @param pDiscardIcon   ImageIcon for the discard button
   * @param pLineNumPanels Array of size 2 with LineNumPanels, index 0 is the LineNumPanel to the left of this ChoiceButtonPane, 1 to the right
   * @param pOrientation   String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   */
  public void addChoiceButtonPanel(@NotNull DiffPanelModel pModel, @NotNull ImageIcon pAcceptIcon, @Nullable ImageIcon pDiscardIcon,
                                   LineNumPanel[] pLineNumPanels, @NotNull String pOrientation)
  {
    ChoiceButtonPanel choiceButtonPanel = new ChoiceButtonPanel(pModel, editorPane, viewPortObservable,
                                                                pAcceptIcon, pDiscardIcon, pLineNumPanels, pOrientation);
    discardables.add(choiceButtonPanel);
    add(choiceButtonPanel, pOrientation.equals(BorderLayout.EAST) ? OnionColumnLayout.RIGHT : OnionColumnLayout.LEFT);
  }

  @Override
  public void discard()
  {
    discardables.forEach(IDiscardable::discard);
  }

  /**
   * "Mapping" from a listener to an Observable of the Rectangle of the JViewPort, Rectangle is the Rectangle that can be seen in the JScrollPane.
   * Coordinates of the Rectangle are in View coordinates
   */
  private class ViewPortObservable extends AbstractListenerObservable<ChangeListener, JViewport, Rectangle>
  {

    ViewPortObservable(@NotNull JViewport pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected ChangeListener registerListener(@NotNull JViewport pJViewport, @NotNull IFireable<Rectangle> pFireable)
    {
      ChangeListener changeListener = e -> {
        Rectangle rect = new Rectangle(pJViewport.getViewRect());
        rect.x = 0;
        pFireable.fireValueChanged(rect);
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
