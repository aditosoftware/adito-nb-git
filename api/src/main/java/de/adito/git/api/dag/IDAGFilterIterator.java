package de.adito.git.api.dag;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * @author m.kaspera, 23.05.2019
 */
public interface IDAGFilterIterator<T extends IDAGObject<T>> extends Iterator<T>
{

  /**
   * Attempts to read pNumEntries from the iterator and passes the values out as list.
   * If the iterator didn't hold as many entries as were demanded, returns the list with as many elements as could be read
   *
   * @param pNumEntries number of entries that should be attempted to read
   * @return List with 0 to pNumEntries, depending on how many could be read
   */
  @NotNull List<T> tryReadEntries(int pNumEntries);

}
