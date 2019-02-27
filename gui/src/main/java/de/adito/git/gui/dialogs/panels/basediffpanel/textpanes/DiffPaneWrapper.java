package de.adito.git.gui.dialogs.panels.basediffpanel.textpanes;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.gui.IDiscardable;
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
import javax.swing.text.EditorKit;
import javax.swing.text.SimpleAttributeSet;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Wrapper around a DiffPane, similar to ForkPointPaneWrapper. Made so that both use a DiffPane for displaying the LineNumPanels/ChoiceButtonPanels
 * while having different functionality in the central editorPane
 *
 * @author m.kaspera, 13.12.2018
 */
public class DiffPaneWrapper implements IDiscardable
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

  /**
   * @param pModel DiffPanelModel that defines what is done when inserting text/how the LineNumbers are retrieved
   */
  public DiffPaneWrapper(DiffPanelModel pModel, Observable<EditorKit> pEditorKitObservable)
  {
    model = pModel;
    editorPane = new JEditorPane();
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
            _textChanged(pFileChangesEvent.getNewValue());
        });
    editorKitDisposable = pEditorKitObservable.subscribe(pEditorKit -> SwingUtilities.invokeLater(() -> _setEditorKit(pEditorKit)));
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

  public boolean isEditorFocusOwner()
  {
    return editorPane.isFocusOwner();
  }

  /**
   * moves the caret to the position of the next changed chunk, as seen from the current position of the caret
   */
  public void moveCaretToNextChunk()
  {
    IDiffPaneUtil.moveCaretToNextChunk(editorPane, model.getFileChangesObservable().blockingFirst().getNewValue(), model.getChangeSide());
  }

  /**
   * moves the caret to the position of the previous changed chunk, as seen from the current position of the caret
   */
  public void moveCaretToPreviousChunk()
  {
    IDiffPaneUtil.moveCaretToPreviousChunk(editorPane, model.getFileChangesObservable().blockingFirst().getNewValue(), model.getChangeSide());
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
    SwingUtilities.invokeLater(() -> editorPane.setCaretPosition(0));
  }

  private void _textChanged(List<IFileChangeChunk> pChangeChunkList)
  {
    final int caretPosition = editorPane.getCaretPosition();
    final Rectangle visibleRect = getScrollPane() != null ? getScrollPane().getVisibleRect() : new Rectangle();
    // insert the text from the IFileDiffs
    TextHighlightUtil.insertColoredText(editorPane,
                                        pChangeChunkList,
                                        model.getChangeSide());
    editorPane.revalidate();
    SwingUtilities.invokeLater(() -> {
      // For whatever reason the EditorCaret thinks it's a good idea to jump to the caret position on text change in a disabled EditorPane,
      // this at least doesn't jump the editor to the bottom of the editor each time and jumps back if the user remembers to set the caret
      editorPane.setCaretPosition(Math.min(caretPosition, editorPane.getDocument().getLength()));
      editorPane.scrollRectToVisible(visibleRect);
    });
  }
}
