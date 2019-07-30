package de.adito.git.impl.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * Utility class with general functions
 *
 * @author m.kaspera, 08.03.2019
 */
public class Util
{

  private Util()
  {
  }

  @Nullable
  public static Throwable getRootCause(@NotNull Exception pE)
  {
    Throwable cause = pE.getCause();
    while (cause != null && cause.getCause() != null)
    {
      cause = cause.getCause();
    }
    return cause;
  }

  /**
   * go through the list and check if the elements in the list are all sorted. Equal objects are always considered to be sorted
   *
   * @param pList       list containing the elements that should be checked for their order
   * @param pComparator Comparator to compare the objects and determine if the ordering of two objects is correct
   * @param <T>         type of the elements contained in the list
   * @return true if the list is sorted, false otherwise
   */
  public static <T> boolean isSorted(List<T> pList, Comparator<T> pComparator)
  {
    for (int index = 0; index < pList.size() - 1; index++)
    {
      if (pComparator.compare(pList.get(index), pList.get(index + 1)) >= 0)
      {
        return false;
      }
    }
    return true;
  }

  /**
   * go through the list and check if the elements in the list are all sorted. Equal objects are always considered to be sorted
   *
   * @param pList       list containing the elements that should be checked for their order
   * @param pComparator Comparator to compare the objects and determine if the ordering of two objects is correct
   * @param <T>         type of the elements contained in the list
   * @return true if the list is sorted in reverse, false otherwise
   */
  public static <T> boolean isReverseSorted(List<T> pList, Comparator<T> pComparator)
  {
    for (int index = 0; index < pList.size() - 1; index++)
    {
      if (pComparator.compare(pList.get(index), pList.get(index + 1)) <= 0)
      {
        return false;
      }
    }
    return true;
  }

}
