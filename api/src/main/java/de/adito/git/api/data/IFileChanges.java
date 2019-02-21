package de.adito.git.api.data;

import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
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
   * @return IFileChangesEvent with the information about what triggered the change
   */
  Observable<IFileChangesEvent> getChangeChunks();

  /**
   * Accepts the changes from the B-side of the IFileChangeChunk, writes them to the A-side, saves
   * the changes into the list and adjusts the line numbers in the following IFileChangeChunks if needed
   *
   * @param pToChangeChunk IFileChangeChunk whose B-side is accepted -> copy B-side over to the A-side
   */
  void applyChanges(@NotNull IFileChangeChunk pToChangeChunk);

  /**
   * Reverts the changes made and resets the changes made in the B-side back to the state of the A-side
   *
   * @param pToChangeChunk IFileChangeChunk whose changes should be reverted
   */
  void resetChanges(@NotNull IFileChangeChunk pToChangeChunk);

  /**
   * Replaces pCurrent in the list provided by getChangeChunks by pReplaceWith
   *
   * @param pCurrent       IFileChangeChunk to replace, should be part of the list in getChangeChunks()
   * @param pReplaceWith   IFileChangeChunk to replace current
   * @param pTriggerUpdate Whether or not the Observable from getChangeChunks should fire a onNext update
   * @return true if current was in the list and could be replaced, false otherwise
   */
  boolean replace(IFileChangeChunk pCurrent, IFileChangeChunk pReplaceWith, boolean pTriggerUpdate);

  /**
   * Retrieves the next changed chunk, as seen from pCurrentChunk, in the list
   *
   * @param pCurrentChunk The current IFileChangeChunk
   * @param pChangeChunks The list of all IFileChangeChunks
   * @return the next changedChunk in the list as seen from pCurrentChunk, or null if no next changed chunk exists
   */
  @Nullable
  static IFileChangeChunk getNextChangedChunk(IFileChangeChunk pCurrentChunk, List<IFileChangeChunk> pChangeChunks)
  {
    IFileChangeChunk nextChunk = null;
    boolean encounteredCurrentChunk = false;
    for (IFileChangeChunk changeChunk : pChangeChunks)
    {
      if (changeChunk.equals(pCurrentChunk))
      {
        encounteredCurrentChunk = true;
      }
      else if (changeChunk.getChangeType() != EChangeType.SAME && encounteredCurrentChunk)
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
   * @param pChangeChunks The list of all IFileChangeChunks
   * @return the previous changedChunk in the list as seen from pCurrentChunk, or null if no previous changed chunk exists
   */
  @Nullable
  static IFileChangeChunk getPreviousChangedChunk(IFileChangeChunk pCurrentChunk, List<IFileChangeChunk> pChangeChunks)
  {
    IFileChangeChunk previousChunk = null;
    boolean encounteredCurrentChunk = false;
    for (int index = pChangeChunks.size() - 1; index >= 0; index--)
    {
      if (pChangeChunks.get(index).equals(pCurrentChunk))
      {
        encounteredCurrentChunk = true;
      }
      else if (pChangeChunks.get(index).getChangeType() != EChangeType.SAME && encounteredCurrentChunk)
      {
        previousChunk = pChangeChunks.get(index);
        break;
      }
    }
    return previousChunk;
  }
}
