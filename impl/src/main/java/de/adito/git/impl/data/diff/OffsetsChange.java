package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.IOffsetsChange;

/**
 * @author m.kaspera, 14.05.2020
 */
public final class OffsetsChange implements IOffsetsChange
{

  private final int textOffset;
  private final int lineOffset;

  public OffsetsChange(int pTextOffset, int pLineOffset)
  {
    textOffset = pTextOffset;
    lineOffset = pLineOffset;
  }

  public IOffsetsChange combineWith(int pTextOffset, int pLineOffset)
  {
    return new OffsetsChange(pTextOffset + textOffset, pLineOffset + lineOffset);
  }

  public int getTextOffset()
  {
    return textOffset;
  }

  public int getLineOffset()
  {
    return lineOffset;
  }
}
