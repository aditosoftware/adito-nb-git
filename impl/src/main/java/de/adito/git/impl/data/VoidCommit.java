package de.adito.git.impl.data;

import de.adito.git.api.data.ICommit;
import lombok.NonNull;

import java.time.Instant;
import java.util.List;

/**
 * Commit that represents an invalid or non-existant commit (e.g. the parent of the inital commit)
 */
final class VoidCommit implements ICommit
{

  private static ICommit instance = null;

  private VoidCommit()
  {
  }

  public static ICommit getInstance()
  {
    if (instance == null)
      instance = new de.adito.git.impl.data.VoidCommit();
    return instance;
  }

  @Override
  public String getAuthor()
  {
    return "";
  }

  @Override
  public String getEmail()
  {
    return "";
  }

  @Override
  public String getCommitter()
  {
    return "";
  }

  @Override
  public Instant getTime()
  {
    return Instant.MIN;
  }

  @Override
  public String getMessage()
  {
    return "";
  }

  @Override
  public String getShortMessage()
  {
    return "";
  }

  @Override
  public String getId()
  {
    return "0000000000000000000000000000000000000000000000000000000000000000";
  }

  @Override
  public @NonNull List<ICommit> getParents()
  {
    return List.of();
  }

  @Override
  public void setParents(@NonNull List<ICommit> pCommits)
  {
    // no parents for an invalid/non-existant commit, so this op does nothing
  }

  @Override
  public boolean equals(Object obj)
  {
    return false;
  }

  @Override
  public int hashCode()
  {
    return getId().hashCode();
  }
}
