package de.adito.git.data;

import de.adito.git.api.ICommit;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author m.kaspera 25.09.2018
 */
public class CommitImpl implements ICommit {

    private RevCommit revCommit;

    public CommitImpl(RevCommit pRevCommit){
        revCommit = pRevCommit;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public String getAuthor() {
        return revCommit.getAuthorIdent().getName();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public String getEmail() {
        return revCommit.getCommitterIdent().getEmailAddress();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public String getCommitter() {
        return revCommit.getCommitterIdent().getName();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public long getTime() {
        return revCommit.getCommitTime();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return revCommit.getFullMessage();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public String getShortMessage() {
        return revCommit.getShortMessage();
    }
}
