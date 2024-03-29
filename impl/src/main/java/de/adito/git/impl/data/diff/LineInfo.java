package de.adito.git.impl.data.diff;

import lombok.*;

/**
 * Pair of two integers for the first and last index of a line
 */
@EqualsAndHashCode
@ToString
final class LineInfo
{

  private final int startIndex;
  private final int endIndex;

  LineInfo(int pStartIndex, int pEndIndex)
  {
    startIndex = pStartIndex;
    endIndex = pEndIndex;
  }

  public int getStartIndex()
  {
    return startIndex;
  }

  public int getEndIndex()
  {
    return endIndex;
  }
}
