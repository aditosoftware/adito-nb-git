package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileDiff;
import io.reactivex.Observable;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author m.kaspera 13.12.2018
 */
public class DiffPanelModel
{

  private final Function<IFileChangeChunk, Integer> getNumLines;
  private final Function<IFileChangeChunk, String> getLines;
  private final Function<IFileChangeChunk, String> getParityLines;
  private final Observable<Optional<IFileDiff>> fileDiffObservable;

  DiffPanelModel(Observable<Optional<IFileDiff>> pFileDiffObservable, Function<IFileChangeChunk, Integer> pGetNumLines,
                 Function<IFileChangeChunk, String> pGetLines, Function<IFileChangeChunk, String> pGetParityLines)
  {
    fileDiffObservable = pFileDiffObservable;
    getNumLines = pGetNumLines;
    getLines = pGetLines;
    getParityLines = pGetParityLines;
  }

  Function<IFileChangeChunk, Integer> getGetNumLines()
  {
    return getNumLines;
  }

  public Function<IFileChangeChunk, String> getGetLines()
  {
    return getLines;
  }

  public Function<IFileChangeChunk, String> getGetParityLines()
  {
    return getParityLines;
  }

  public Observable<Optional<IFileDiff>> getFileDiff()
  {
    return fileDiffObservable;
  }

}
