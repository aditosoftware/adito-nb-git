package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.EConflictSide;

/**
 * Stores a pair of indices that give the indices of conflicting deltas in their respective lists of deltas (e.g. index 4 of YOURS is in conflict with index 3 of THEIRS)
 *
 * @author m.kaspera, 23.04.2020
 */
public class ConflictPair
{

  int indexYours;
  int indexTheirs;


  ConflictPair(int pIndexYours, int pIndexTheirs)
  {
    indexYours = pIndexYours;
    indexTheirs = pIndexTheirs;
  }

  /**
   * returns the index for the given conflictSide
   *
   * @param pConflictSide Which conflictSide
   * @return the index for the given conflictSide
   */
  public int getIndexOfSide(EConflictSide pConflictSide)
  {
    if (pConflictSide == EConflictSide.YOURS)
    {
      return indexYours;
    }
    return indexTheirs;
  }
}
