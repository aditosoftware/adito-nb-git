package de.adito.git.impl.data;

import de.adito.git.api.data.ICommit;
import lombok.NonNull;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 25.09.2018
 */
public class CommitImpl implements ICommit
{

  private final RevCommit revCommit;
  private List<ICommit> parents = null;
  public static final ICommit VOID_COMMIT = VoidCommit.getInstance();

  public CommitImpl(@NonNull RevCommit pRevCommit)
  {
    revCommit = pRevCommit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAuthor()
  {
    return revCommit.getAuthorIdent().getName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getEmail()
  {
    return revCommit.getAuthorIdent().getEmailAddress();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCommitter()
  {
    return revCommit.getCommitterIdent().getName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Instant getTime()
  {
    return revCommit.getCommitterIdent().getWhen().toInstant();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMessage()
  {
    return revCommit.getFullMessage();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getShortMessage()
  {
    return revCommit.getShortMessage();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId()
  {
    return ObjectId.toString(revCommit.getId());
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public List<ICommit> getParents()
  {
    if (parents != null)
      return parents;
    if (revCommit.getParents() != null)
      return Arrays.stream(revCommit.getParents()).map(CommitImpl::new).collect(Collectors.toList());
    return Collections.emptyList();
  }

  @Override
  public void setParents(@NonNull List<ICommit> pCommits)
  {
    parents = pCommits;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
      return false;
    else if (!(obj instanceof ICommit))
      return false;
    else return ((ICommit) obj).getId().equals(getId());
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(revCommit.getId());
  }
}
