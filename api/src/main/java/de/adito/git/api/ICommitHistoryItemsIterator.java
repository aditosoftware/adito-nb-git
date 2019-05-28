package de.adito.git.api;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * @author m.kaspera, 28.05.2019
 */
public interface ICommitHistoryItemsIterator extends Iterator<CommitHistoryTreeListItem>
{

  /**
   * Attempts to read pNumEntries from the iterator and passes the values out as list.
   * If the iterator didn't hold as many entries as were demanded, returns the list with as many elements as could be read
   *
   * @param pNumEntries number of entries that should be attempted to read
   * @return List with 0 to pNumEntries, depending on how many could be read
   */
  @NotNull List<CommitHistoryTreeListItem> tryReadEntries(int pNumEntries);
}
