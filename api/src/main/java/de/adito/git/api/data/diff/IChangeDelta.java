package de.adito.git.api.data.diff;

import java.util.List;

/**
 * @author m.kaspera, 20.02.2020
 */
public interface IChangeDelta
{

  /**
   * @return the type of change that occurred
   */
  IChangeStatus getChangeStatus();

  /**
   * get the number of the first line that is affected by this change
   *
   * @param pChangeSide which side of the change should be taken
   * @return line that the change on the chosen side starts
   */
  int getStartLine(EChangeSide pChangeSide);

  /**
   * get the number of the last line that is affected by this change
   *
   * @param pChangeSide which side of the change should be taken
   * @return line that the change on the chosen side ends
   */
  int getEndLine(EChangeSide pChangeSide);

  /**
   * Get the index of the first character of this chunk, the index should be the same as in a document containing all changeDeltas of a fileChange
   *
   * @param pChangeSide which side of the change should be taken
   * @return index of the first character of this chunk as seen for the whole file
   */
  int getStartTextIndex(EChangeSide pChangeSide);

  /**
   * Get the index of the last character of this chunk, the index should be the same as in a document containing all changeDeltas of a fileChange
   *
   * @param pChangeSide which side of the change should be taken
   * @return index of the last character  of this chunk as seen for the whole file
   */
  int getEndTextIndex(EChangeSide pChangeSide);

  /**
   * applies the given offsets to the stored offsets of this delta
   *
   * @param pLineOffset offset for the lines
   * @param pTextOffset offset for the text indices
   * @return IChangeDelta that has its values updated with the given offsets
   */
  IChangeDelta applyOffset(int pLineOffset, int pTextOffset);

  /**
   * Get a list of changes of this delta on a word-basis
   *
   * @return List describing the changes of this delta on a word-basis
   */
  List<IChangeDelta> getWordChanges();

  /**
   * Accepts the changes in this delta and returns a new IChangeDelta with the changed attributes
   *
   * @return new IChangeDelta
   */
  IChangeDelta acceptChange();

  /**
   * Discards the Changes of this delta and returns a new IChangeDelta with the changed attributes
   *
   * @return new IChangeDelta
   */
  IChangeDelta discardChange();
}
