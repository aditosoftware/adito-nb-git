package de.adito.git.api.data;

/**
 * Interface for a chunk of a file that was changed, i.e.:
 * INSERT (3,3), (3,4) "" "this line was inserted"
 * -> insert the line "this line was inserted" after line 3 on the B file
 * INSERT                       getChangeType
 * (3,3)                        getAStart, getAEnd
 * (3,4)                        getBStart, getBEnd
 * ""                           getALines
 * "this line was inserted"     getBLines
 *
 * @author m.kaspera 05.10.2018
 */
public interface IFileChangeChunk {

    /**
     *
     * @return line that the change on side A starts
     */
    int getAStart();

    /**
     *
     * @return line that the change on side A ends
     */
    int getAEnd();

    /**
     *
     * @return line that the change on side B starts
     */
    int getBStart();

    /**
     *
     * @return line that the change on side B ends
     */
    int getBEnd();

    /**
     *
     * @return the type of change that occurred
     */
    EChangeType getChangeType();

    /**
     *
     * @return the contents of the line that were changed on A side (empty String for insert)
     */
    String getALines();

    /**
     *
     * @return the contents of the line that were changed on B side (empty String for remove)
     */
    String getBLines();

    String getAParityLines();

    String getBParityLines();
}
