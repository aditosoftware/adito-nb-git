package de.adito.git.api.data;

import de.adito.git.api.data.diff.EChangeStatus;
import de.adito.git.api.data.diff.IChangeDelta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Keeps an observable list of IFileChangeChunks and provided methods that operate on that list
 *
 * @author m.kaspera 25.09.2018
 */
public interface IFileChanges
{

  /**
   * Replaces pCurrent in the list provided by getChangeChunks by pReplaceWith
   *
   * @param pCurrent       IFileChangeChunk to replace, should be part of the list in getChangeChunks()
   * @param pReplaceWith   IFileChangeChunk to replace current
   * @param pTriggerUpdate Whether or not the Observable from getChangeChunks should fire a onNext update
   * @return true if current was in the list and could be replaced, false otherwise
   */
  boolean replace(IChangeDelta pCurrent, IChangeDelta pReplaceWith, boolean pTriggerUpdate);

  /**
   * Retrieves the next changed chunk, as seen from pCurrentChunk, in the list
   *
   * @param pCurrentChunk The current IFileChangeChunk
   * @param pChangeDeltas The list of all IFileChangeChunks
   * @return the next changedChunk in the list as seen from pCurrentChunk, or null if no next changed chunk exists
   */
  @Nullable
  static IChangeDelta getNextChangedChunk(IChangeDelta pCurrentChunk, List<IChangeDelta> pChangeDeltas)
  {
    IChangeDelta nextChunk = null;
    boolean encounteredCurrentChunk = false;
    for (IChangeDelta changeChunk : pChangeDeltas)
    {
      if (changeChunk.equals(pCurrentChunk))
      {
        encounteredCurrentChunk = true;
      }
      else if (changeChunk.getChangeStatus().getChangeStatus() == EChangeStatus.PENDING && encounteredCurrentChunk)
      {
        nextChunk = changeChunk;
        break;
      }
    }
    return nextChunk;
  }

  /**
   * Retrieves the previous changed chunk, as seen from pCurrentChunk, in the list
   *
   * @param pCurrentChunk The current IFileChangeChunk
   * @param pChangeDeltas The list of all IFileChangeChunks
   * @return the previous changedChunk in the list as seen from pCurrentChunk, or null if no previous changed chunk exists
   */
  @Nullable
  static IChangeDelta getPreviousChangedChunk(IChangeDelta pCurrentChunk, List<IChangeDelta> pChangeDeltas)
  {
    IChangeDelta previousChunk = null;
    boolean encounteredCurrentChunk = false;
    for (int index = pChangeDeltas.size() - 1; index >= 0; index--)
    {
      if (pChangeDeltas.get(index).equals(pCurrentChunk))
      {
        encounteredCurrentChunk = true;
      }
      else if (pChangeDeltas.get(index).getChangeStatus().getChangeStatus() == EChangeStatus.PENDING && encounteredCurrentChunk)
      {
        previousChunk = pChangeDeltas.get(index);
        break;
      }
    }
    return previousChunk;
  }
}
