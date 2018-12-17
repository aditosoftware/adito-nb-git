package de.adito.git.api.data;

import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Keeps an observable list of IFileChangeChunks and provided methods that operate on that list
 *
 * @author m.kaspera 25.09.2018
 */
public interface IFileChanges
{

  /**
   * @return List with the changes made to the file, organized in chunks
   */
  Observable<List<IFileChangeChunk>> getChangeChunks();

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
   * @param pCurrent     IFileChangeChunk to replace, should be part of the list in getChangeChunks()
   * @param pReplaceWith IFileChangeChunk to replace current
   * @return true if current was in the list and could be replaced, false otherwise
   */
  boolean replace(IFileChangeChunk pCurrent, IFileChangeChunk pReplaceWith);
}
