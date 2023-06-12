package de.adito.git.impl.dag;

import de.adito.git.api.dag.IDAGFilterIterator;
import de.adito.git.api.dag.IDAGObject;
import lombok.NonNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author m.kaspera, 22.05.2019
 */
public class DAGFilterIterator<T extends IDAGObject<T>> implements IDAGFilterIterator<T>
{

  private final LinkedList<T> elementQueue = new LinkedList<>();
  private final Iterator<T> iterator;
  private final Predicate<T> filterPredicate;

  public DAGFilterIterator(Iterator<T> pIterator, Predicate<T> pFilterPredicate)
  {
    iterator = pIterator;
    filterPredicate = pFilterPredicate;
  }

  @Override
  public boolean hasNext()
  {
    while (iterator.hasNext() && elementQueue.isEmpty())
    {
      _acceptNext(iterator.next());
    }
    return !elementQueue.isEmpty();
  }

  @Override
  public T next()
  {
    if (!elementQueue.isEmpty() && _isParentsMatch(elementQueue.peekFirst()))
      return elementQueue.poll();
    while (iterator.hasNext() && !_isParentsMatch(elementQueue.peekFirst()))
    {
      _acceptNext(iterator.next());
    }
    return elementQueue.poll();
  }

  /**
   * adds the next element to the elementQueue if it fits the predicate, else swaps all it occurrances with its parents
   *
   * @param pElement next Element from the DAG
   */
  private void _acceptNext(T pElement)
  {
    if (filterPredicate.test(pElement))
    {
      elementQueue.add(pElement);
    }
    else
    {
      for (T queueElement : elementQueue)
      {
        _swapIfApplicable(queueElement, pElement);
      }
    }
  }

  /**
   * replace pSwapWithParents with its parents if it is a parent of pQueueElement
   *
   * @param pQueueElement    element from the queue
   * @param pSwapWithParents element that should be replaced with its parents if contained in the parentList of pQueueElement
   */
  private void _swapIfApplicable(T pQueueElement, T pSwapWithParents)
  {
    if (pQueueElement.getParents().contains(pSwapWithParents))
    {
      pQueueElement.setParents(_replace(pQueueElement.getParents(), pSwapWithParents, pSwapWithParents.getParents()));
    }
  }

  /**
   * replace pElementToReplace in pOriginalList with pReplaceWith. Elements of pReplaceWith will be inserted in the position pElementToReplace was at,
   * if the size of pReplaceWith is bigger 1 all elements behind pElementToReplace will be moved back
   *
   * @param pOriginalList     List to perform the replace operation on
   * @param pElementToReplace element to replace
   * @param pReplaceWith      list of objects that should be put into place instead of pElementToReplace
   * @return pOriginalList that has pElementToReplace replaced with pReplaceWith
   */
  private List<T> _replace(List<T> pOriginalList, T pElementToReplace, List<T> pReplaceWith)
  {
    // use a set here to avoid puttting duplicates into the list
    Set<T> listCopy = new HashSet<>();
    for (T originalElem : pOriginalList)
    {
      if (!originalElem.equals(pElementToReplace))
      {
        listCopy.add(originalElem);
      }
      else
      {
        listCopy.addAll(pReplaceWith);
      }
    }
    return new ArrayList<>(listCopy);
  }

  /**
   * determines if the parents of pElement pass the predicate test
   *
   * @param pElement Element whose parents should be checked
   * @return true if ALL parents pass the predicate test
   */
  private boolean _isParentsMatch(T pElement)
  {
    if (pElement == null)
      return false;
    if (pElement.getParents() == null)
      return true;
    for (T parent : pElement.getParents())
    {
      if (!filterPredicate.test(parent))
        return false;
    }
    return true;
  }

  @Override
  public @NonNull List<T> tryReadEntries(int pNumEntries)
  {
    List<T> entries = new ArrayList<>(pNumEntries);
    for (int index = 0; index < pNumEntries && hasNext(); index++)
    {
      entries.add(next());
    }
    return entries;
  }
}
