package de.adito.git.gui.dialogs.panels.TextPanes;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.TextHighlightUtil;
import de.adito.git.gui.dialogs.panels.DiffPanelModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * @author m.kaspera, 13.12.2018
 */
public class DiffTextPaneWrapper implements IDiscardable
{

  private final JTextPane textPane;
  private final DiffPanelModel model;
  private final Disposable disposable;

  public DiffTextPaneWrapper(DiffPanelModel pModel)
  {
    model = pModel;
    textPane = new NonWrappingTextPane();
    disposable = model.getFileDiff().switchMap(pFileDiff -> pFileDiff
        .map(pDiff -> pDiff.getFileChanges().getChangeChunks())
        .orElse(Observable.just(Collections.emptyList())))
        .subscribe(this::_textChanged);
  }

  public JTextPane getTextPane()
  {
    return textPane;
  }

  private void _textChanged(List<IFileChangeChunk> pChangeChunkList)
  {
    final int caretPosition = textPane.getCaretPosition();
    // insert the text from the IFileDiffs
    TextHighlightUtil.insertColoredText(textPane, pChangeChunkList, model.getGetLines(), model.getGetParityLines());
    textPane.setCaretPosition(caretPosition);
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }
}
