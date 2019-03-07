package de.adito.git.api.data;

/**
 * Interface for a chunk of a file that was changed, i.e.:
 * INSERT (3,3), (3,4) "" "this line was inserted"
 * {@code -> insert the line "this line was inserted" after line 3 on the B file}
 * INSERT                       getChangeType
 * (3,3)                        getAStart, getAEnd
 * (3,4)                        getBStart, getBEnd
 * ""                           getALines
 * "this line was inserted"     getBLines
 *
 * @author m.kaspera 05.10.2018
 */
public interface IFileChangeChunk
{

  /**
   * get the number of the first line that is affected by this change
   *
   * @param pChangeSide which side of the change should be taken
   * @return line that the change on the chosen side starts
   */
  int getStart(EChangeSide pChangeSide);

  /**
   * get the number of the last line that is affected by this change
   *
   * @param pChangeSide which side of the change should be taken
   * @return line that the change on the chosen side ends
   */
  int getEnd(EChangeSide pChangeSide);

  /**
   * @return the type of change that occurred
   */
  EChangeType getChangeType();

  /**
   * get the lines in the scope of this chunk, either as they were or are after the change, depending on the passed EChangeSide
   *
   * @param pChangeSide which side of the change should be taken
   * @return the contents of the line that were changed on the chosen side (empty String for insert)
   */
  String getLines(EChangeSide pChangeSide);

  /**
   * get the lines in the scope of this chunk, either as they were or after the change, depending on the passed EChangeSide
   * returns the lines in standardised format with only \n as newline and not in the 1230194823962346 different other ways to represent newlines
   *
   * @param pChangeSide which side of the change should be taken
   * @return the contents of the line that were changed on the chosen side (empty String for insert)
   */
  String getEditorLines(EChangeSide pChangeSide);

}
