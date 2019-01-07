package de.adito.git.gui.dialogs.panels.TextPanes;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.gui.*;
import de.adito.git.gui.dialogs.panels.DiffPanelModel;
import de.adito.git.impl.data.FileChangesEventImpl;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.util.*;

/**
 * @author m.kaspera, 13.12.2018
 */
public class DiffPaneWrapper implements IDiscardable
{

  private final JScrollPane textScrollPane;
  private final JEditorPane textPane;
  private final DiffPanelModel model;
  private final CompositeDisposable disposable = new CompositeDisposable();

  public DiffPaneWrapper(DiffPanelModel pModel, @Nullable IEditorKitProvider pEditorKitProvider)
  {
    model = pModel;
    textPane = new NonWrappingTextPane();

    //add the editor kit to the textpane to show the mimetype of a file in the editor window
    disposable.add(pModel.getFileDiff().subscribe(pFileDiffOpt -> {
      EditorKit kit = pFileDiffOpt
          .map(pDiff -> pEditorKitProvider == null ? null : pEditorKitProvider.getEditorKit(pDiff.getFilePath()))
          .orElse(null);

      String oldText = textPane.getText();
      textPane.setEditorKit(kit);
      textPane.setText(oldText);
    }));

    textScrollPane = new JScrollPane(textPane);
    disposable.add(model.getFileDiff().switchMap(pFileDiff -> pFileDiff
        .map(pDiff -> pDiff.getFileChanges().getChangeChunks())
        .orElse(Observable.just(new FileChangesEventImpl(true, Collections.emptyList()))))
                       .subscribe(pFileChangesEvent -> {
                         if (pFileChangesEvent.isUpdateUI())
                           _textChanged(pFileChangesEvent.getNewValue());
                       }));
  }

  public JScrollPane getPane()
  {
    return textScrollPane;
  }

  public JEditorPane getTextPane()
  {
    return textPane;
  }

  private void _textChanged(List<IFileChangeChunk> pChangeChunkList)
  {
    final int caretPosition = textPane.getCaretPosition();
    // insert the text from the IFileDiffs
    TextHighlightUtil.insertColoredText(textPane, pChangeChunkList, model.getGetLines(), model.getGetParityLines());
    textPane.revalidate();
    SwingUtilities.invokeLater(() -> textPane.setCaretPosition(caretPosition));
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }
}
