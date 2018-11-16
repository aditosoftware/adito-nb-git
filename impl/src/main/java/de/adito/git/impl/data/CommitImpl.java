package de.adito.git.impl.data;

import de.adito.git.api.data.ICommit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    public Date getTime() {
        return revCommit.getCommitterIdent().getWhen();
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

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return ObjectId.toString(revCommit.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICommit> getParents() {
        return Arrays.stream(revCommit.getParents()).map(CommitImpl::new).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        else if (!(obj instanceof ICommit))
            return false;
        else return ((ICommit) obj).getId().equals(getId());
    }
}
