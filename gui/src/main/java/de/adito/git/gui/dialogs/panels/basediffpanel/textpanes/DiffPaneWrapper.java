package de.adito.git.gui.dialogs.panels.basediffpanel.textpanes;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.gui.TextHighlightUtil;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import de.adito.git.gui.dialogs.panels.basediffpanel.IDiffPaneUtil;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.DiffPane;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.MarkedScrollbar;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.ScrollbarMarkingsModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.openide.modules.Modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Wrapper around a DiffPane, similar to ForkPointPaneWrapper. Made so that both use a DiffPane for displaying the LineNumPanels/ChoiceButtonPanels
 * while having different functionality in the central editorPane
 *
 * @author m.kaspera, 13.12.2018
 */
public class DiffPaneWrapper implements IDiscardable, IPaneWrapper
{


  private static final String LIB2_CLASSLOADER_NAME = "org.netbeans.modules.editor.lib2";
  private static final String HIGHLIGHTING_MANAGER_CLASS_NAME = "org.netbeans.modules.editor.lib2.highlighting.HighlightingManager";
  private static final String CARET_BASED_HIGHLIGHTING_CLASS_NAME = "org.netbeans.modules.editor.lib2.highlighting.CaretBasedBlockHighlighting";
  private static final String CARET_BASED_ROW_HIGHLIGHTS_CLASS_NAME = "org.netbeans.modules.editor.lib2.highlighting." +
      "CaretBasedBlockHighlighting$CaretRowHighlighting";
  private final JEditorPane editorPane;
  private final DiffPane diffPane;
  private final DiffPanelModel model;
  private final Disposable fileChangeDisposable;
  private final Disposable editorKitDisposable;
  private final ScrollbarMarkingsModel scrollbarMarkingsModel;
  private Field attribsFields;
  private Object attribsParentLayer;
  private int cachedListHash = 0;

  /**
   * @param pModel DiffPanelModel that defines what is done when inserting text/how the LineNumbers are retrieved
   */
  public DiffPaneWrapper(DiffPanelModel pModel, Observable<Optional<EditorKit>> pEditorKitObservable)
  {
    model = pModel;
    editorPane = new JEditorPane();
    editorPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    editorPane.addFocusListener(new FocusListener()
    {

      @Override
      public void focusGained(FocusEvent pFocusEvent)
      {
        editorPane.getCaret().setVisible(true);
      }

      @Override
      public void focusLost(FocusEvent pFocusEvent)
      {
        editorPane.getCaret().setVisible(false);
      }
    });
    editorPane.setEditable(false);
    diffPane = new DiffPane(editorPane);
    fileChangeDisposable = model.getFileChangesObservable()
        .subscribe(pFileChangesEvent -> {
          if (pFileChangesEvent.isUpdateUI())
          {
            if (cachedListHash != System.identityHashCode(pFileChangesEvent.getNewValue()))
              editorPane.setText("");
            cachedListHash = System.identityHashCode(pFileChangesEvent.getNewValue());
            _textChanged(pFileChangesEvent);
          }
        });
    editorKitDisposable = pEditorKitObservable.subscribe(pOptEditorKit
                                                             -> pOptEditorKit.ifPresent(pEditorKit -> SwingUtilities.invokeLater(() -> _setEditorKit(pEditorKit))));
    MarkedScrollbar markedScrollbar = new MarkedScrollbar();
    getScrollPane().setVerticalScrollBar(markedScrollbar);
    scrollbarMarkingsModel = new ScrollbarMarkingsModel(pModel, editorPane, markedScrollbar);
  }

  /**
   * @return JScrollPane with the content of this DiffPane, add this to your panel
   */
  public JScrollPane getScrollPane()
  {
    return diffPane.getScrollPane();
  }

  /**
   * @return DiffPane that this wrapper is made for, only use this to add LineNumber/ChoiceButtonPanels. Add the JScrollPane via getScrollPane() to
   * the panel/component that should display the DiffPane
   */
  public DiffPane getPane()
  {
    return diffPane;
  }

  /**
   * @return the JEditorPane displaying the text for this DiffPaneWrapper
   */
  public JEditorPane getEditorPane()
  {
    return editorPane;
  }

  public boolean isEditorFocusOwner()
  {
    return editorPane.isFocusOwner();
  }

  /**
   * moves the caret to the position of the next changed chunk, as seen from the current position of the caret
   *
   * @param pTextPane textPane that displays the other side of the diff
   */
  public void moveCaretToNextChunk(JTextComponent pTextPane)
  {
    IFileChangeChunk nextChunk = IDiffPaneUtil.getNextChunk(editorPane, model.getFileChangesObservable().blockingFirst().getNewValue(), model.getChangeSide());
    _moveCaretToChunk(pTextPane, nextChunk);
  }

  /**
   * moves the caret to the position of the previous changed chunk, as seen from the current position of the caret
   *
   * @param pTextPane textPane that displays the other side of the diff
   */
  public void moveCaretToPreviousChunk(JTextComponent pTextPane)
  {
    IFileChangeChunk previousChunk = IDiffPaneUtil.getPreviousChunk(editorPane, model.getFileChangesObservable().blockingFirst().getNewValue(), model.getChangeSide());
    _moveCaretToChunk(pTextPane, previousChunk);
  }

  /**
   * moves the caret to the position of the given chunk
   *
   * @param pTextPane textPane that displays the other side of the diff
   * @param pChunk chunk that the caret should be moved to
   */
  private void _moveCaretToChunk(JTextComponent pTextPane, IFileChangeChunk pChunk)
  {
    IDiffPaneUtil.moveCaretToChunk(editorPane, pChunk, model.getChangeSide());
    IDiffPaneUtil.moveCaretToChunk(pTextPane, pChunk, model.getChangeSide() == EChangeSide.NEW ? EChangeSide.OLD : EChangeSide.NEW);
    editorPane.requestFocus();
  }

  @Override
  public void discard()
  {
    diffPane.discard();
    fileChangeDisposable.dispose();
    editorKitDisposable.dispose();
    scrollbarMarkingsModel.discard();
  }

  private void _setEditorKit(EditorKit pEditorKit)
  {
    String currentText = editorPane.getText();
    editorPane.setEditorKit(pEditorKit);
    editorPane.setText(currentText);
    editorPane.setCaretPosition(0);
    getScrollPane().getVerticalScrollBar().setValue(0);

    _setNoCaretLineHighlights();
  }

  /**
   * Sets the attributes responsible for coloring the line the caret is in to EMPTY
   * only works via reflections, so whack-a-mole reflection style it is
   */
  private void _setNoCaretLineHighlights()
  {
    try
    {
      ClassLoader clazzLoader = Modules.getDefault().findCodeNameBase(LIB2_CLASSLOADER_NAME).getClassLoader();
      Class<?> hlmanager = Class.forName(HIGHLIGHTING_MANAGER_CLASS_NAME, false, clazzLoader);
      Object manager = editorPane.getClientProperty(hlmanager);
      Field highlighting = hlmanager.getDeclaredField("highlighting");
      highlighting.setAccessible(true);
      Object highlightingObj = highlighting.get(manager);
      Field topHighlights = highlightingObj.getClass().getDeclaredField("topHighlights");
      topHighlights.setAccessible(true);
      Object topHighlightsObj = topHighlights.get(highlightingObj);
      Field layers = topHighlightsObj.getClass().getDeclaredField("layers");
      layers.setAccessible(true);
      Object[] layersArr = (Object[]) layers.get(topHighlightsObj);
      for (Object layer : layersArr)
      {
        Class<?> cbbhClazz = Class.forName(CARET_BASED_HIGHLIGHTING_CLASS_NAME, false, clazzLoader);
        Class<?> crhClazz = Class.forName(CARET_BASED_ROW_HIGHLIGHTS_CLASS_NAME,
                                          false, clazzLoader);
        if (layer != null && cbbhClazz.isAssignableFrom(layer.getClass()) && layer.getClass().equals(crhClazz))
        {
          attribsParentLayer = layer;
          attribsFields = cbbhClazz.getDeclaredField("attribs");
          attribsFields.setAccessible(true);
          attribsFields.set(layer, SimpleAttributeSet.EMPTY);
        }
      }
      // the actual whack-a-mole part: each time the caret is set, the attribs would be set to an actual color, so we set them back to EMPTY
      editorPane.getCaret().addChangeListener(e -> {
        try
        {
          if (attribsFields != null)
            attribsFields.set(attribsParentLayer, SimpleAttributeSet.EMPTY);
        }
        catch (Exception ex)
        {
          throw new RuntimeException(ex);
        }
      });
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private void _textChanged(IFileChangesEvent pChangesEvent)
  {
    // insert the text from the event
    TextHighlightUtil.insertColoredText(editorPane,
                                        pChangesEvent,
                                        model.getChangeSide());
    SwingUtilities.invokeLater(() -> {
      editorPane.revalidate();
      editorPane.repaint();
    });
  }
}
