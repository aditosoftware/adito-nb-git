package de.adito.git.api.data;

import de.adito.git.api.dag.IDAGObject;
import lombok.NonNull;

import java.time.Instant;
import java.util.List;

/**
 * Interface for the functionality of a Object that has the details of a commit
 *
 * @author m.kaspera 25.09.2018
 */
public interface ICommit extends IDAGObject<ICommit>
{

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
    @NonNull
    List<ICommit> getParents();

    /**
     * overrides the parents of this commit, can be used to make a DAG into a dense DAG
     *
     * @param pCommits list of commits that should be given out as parents
     */
    void setParents(@NonNull List<ICommit> pCommits);
}
