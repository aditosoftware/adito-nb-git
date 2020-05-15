package de.adito.git.impl.data.diff;

/**
 * Combines information about the start- and endTextOffsets of the two sides of a diff into one immutable data object
 *
 * @author m.kaspera, 27.02.2020
 */
final class ChangeDeltaTextOffsets
{

  private final int startIndexOriginal;
  private final int endIndexOriginal;
  private final int startIndexChanged;
  private final int endIndexChanged;

  public ChangeDeltaTextOffsets(int pStartIndexOriginal, int pEndIndexOriginal, int pStartIndexChanged, int pEndIndexChanged)
  {
    startIndexOriginal = pStartIndexOriginal;
    endIndexOriginal = pEndIndexOriginal;
    startIndexChanged = pStartIndexChanged;
    endIndexChanged = pEndIndexChanged;
  }

  public int getStartIndexOriginal()
  {
    return startIndexOriginal;
  }

  public int getEndIndexOriginal()
  {
    return endIndexOriginal;
  }

  public int getStartIndexChanged()
  {
    return startIndexChanged;
  }

  public int getEndIndexChanged()
  {
    return endIndexChanged;
  }

  /**
   * Creates a new ChangeDetlaTextOffsets object with the applied offsets
   *
   * @param pOffsetOriginal offset added to the start and endOffset of the original side
   * @param pOffsetChanged  offset added to the start and endOffset of the changed  side
   * @return new ChangeDeltaTextOffsets object
   */
  public ChangeDeltaTextOffsets applyOffset(int pOffsetOriginal, int pOffsetChanged)
  {
    return new ChangeDeltaTextOffsets(startIndexOriginal + pOffsetOriginal, endIndexOriginal + pOffsetOriginal,
                                      startIndexChanged + pOffsetChanged, endIndexChanged + pOffsetChanged);
  }

}
