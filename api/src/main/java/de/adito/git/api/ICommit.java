package de.adito.git.api;

import java.util.Date;

/**
 * Interface for the functionality of a Object that has the details of a commit
 *
 * @author m.kaspera 25.09.2018
 */
public interface ICommit {

    /**
     *
     * @return the Author of the commit as String
     */
    String getAuthor();

    /**
     *
     * @return the email of the committer
     */
    String getEmail();

    /**
     *
     * @return the name of the committer as String
     */
    String getCommitter();

    /**
     *
     * @return the timestamp of the date of the commit
     */
    Date getTime();

    /**
     *
     * @return the whole message written for the commit as String
     */
    String getMessage();

    /**
     *
     * @return the first line of the commit message
     */
    String getShortMessage();
}
