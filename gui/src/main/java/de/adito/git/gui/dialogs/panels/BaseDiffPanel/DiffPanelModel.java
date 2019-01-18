package de.adito.git.gui.dialogs.panels.BaseDiffPanel;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
import io.reactivex.Observable;

import java.util.function.Function;

/**
 * Model for the Panels in the DiffPanel/MergePanel
 *
 * @author m.kaspera 13.12.2018
 */
public class DiffPanelModel
{

  private final Function<IFileChangeChunk, String> getLines;
  private final Function<IFileChangeChunk, String> getParityLines;
  private final Function<IFileChangeChunk, Integer> getStartLine;
  private final Function<IFileChangeChunk, Integer> getEndLine;
  private final Observable<IFileChangesEvent> fileChangesObservable;

  /**
   * @param pFileChangesObservable Observable of the IFileChangesEvents of the IFileDiff this model is for
   * @param pGetLines              Function whose apply Function returns the lines of one side (specified when the Function was created) for a
   *                               given IFileChangeChunk
   * @param pGetParityLines        Function whose apply Function returns the parityLines of one side (specified when the Function was created) for a
   *                               given IFileChangeChunk
   * @param pGetStartLine          Function whose apply method returns the starting line of the (previously) specified side of the IFileChangeChunk
   * @param pGetEndLine            Function whose apply method returns the end line (excluded) of the (previously) specified side of the
   *                               IFileChangeChunk
   */
  DiffPanelModel(Observable<IFileChangesEvent> pFileChangesObservable, Function<IFileChangeChunk, String> pGetLines,
                 Function<IFileChangeChunk, String> pGetParityLines, Function<IFileChangeChunk, Integer> pGetStartLine,
                 Function<IFileChangeChunk, Integer> pGetEndLine)
  {
    fileChangesObservable = pFileChangesObservable;
    getLines = pGetLines;
    getParityLines = pGetParityLines;
    getStartLine = pGetStartLine;
    getEndLine = pGetEndLine;
  }

  /**
   * @return Function whose apply Function returns the lines of one side (specified when the Function was created) for a given IFileChangeChunk
   */
  public Function<IFileChangeChunk, String> getGetLines()
  {
    return getLines;
  }

  /**
   * @return Function whose apply Function returns the parityLines of one side (specified when the Function was created) for a given IFileChangeChunk
   */
  public Function<IFileChangeChunk, String> getGetParityLines()
  {
    return getParityLines;
  }

  public Observable<IFileChangesEvent> getFileChangesObservable()
  {
    return fileChangesObservable;
  }

  /**
   * @return Function whose apply method returns the starting line of the (previously) specified side of the IFileChangeChunk
   */
  public Function<IFileChangeChunk, Integer> getGetStartLine()
  {
    return getStartLine;
  }

  /**
   * @return Function whose apply method returns the end line (excluded) of the (previously) specified side of the IFileChangeChunk
   */
  public Function<IFileChangeChunk, Integer> getGetEndLine()
  {
    return getEndLine;
  }
}
