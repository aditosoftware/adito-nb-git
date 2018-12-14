package de.adito.git.api.data;

import java.time.Instant;
import java.util.Date;
import java.util.List;

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
    Instant getTime();

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

    /**
     *
     * @return the ID of the Commit as String
     */
    String getId();

    /**
     * @return List of ICommits that form the parents of this ICommit
     */
    List<ICommit> getParents();
}
