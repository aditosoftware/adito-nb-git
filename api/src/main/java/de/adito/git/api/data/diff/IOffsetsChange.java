package de.adito.git.api.data.diff;

/**
 * Represents a change in offsets that a changeDelta or LinePartChangeDelta can undergo, stored are both text and line offset
 *
 * @author m.kaspera, 14.05.2020
 */
public interface IOffsetsChange
{

  /**
   * combines this offsetsChange with the passed offset parameters (simple addition)
   *
   * @param pTextOffset additional text offset
   * @param pLineOffset additional line offset
   * @return new IOffsetsChange with the combined offsets
   */
  IOffsetsChange combineWith(int pTextOffset, int pLineOffset);

  /**
   * @return offset of the text component
   */
  int getTextOffset();

  /**
   * @return offset of the lines
   */
  int getLineOffset();

}
