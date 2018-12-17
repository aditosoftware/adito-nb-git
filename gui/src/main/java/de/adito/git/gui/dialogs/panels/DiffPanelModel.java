package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.*;
import io.reactivex.Observable;

import java.util.Optional;
import java.util.function.Function;

public class DiffPanelModel
{

  private final Function<IFileChangeChunk, Integer> getNumLines;
  private final Function<IFileChangeChunk, String> getParityLines;
  private final boolean useParityLines;
  private final Observable<Optional<IFileDiff>> fileDiffObservable;

  protected DiffPanelModel(Observable<Optional<IFileDiff>> pFileDiffObservable, Function<IFileChangeChunk, Integer> pGetNumLines,
                           Function<IFileChangeChunk, String> pGetParityLines, boolean pUseParityLines)
  {
    fileDiffObservable = pFileDiffObservable;
    getNumLines = pGetNumLines;
    getParityLines = pGetParityLines;
    useParityLines = pUseParityLines;
  }

  public Function<IFileChangeChunk, Integer> getGetNumLines()
  {
    return getNumLines;
  }

  public Function<IFileChangeChunk, String> getGetParityLines()
  {
    return getParityLines;
  }

  public boolean isUseParityLines()
  {
    return useParityLines;
  }

  public Observable<Optional<IFileDiff>> getFileDiff()
  {
    return fileDiffObservable;
  }
}
