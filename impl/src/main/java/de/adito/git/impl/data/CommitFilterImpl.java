package de.adito.git.impl.data;

import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.ICommitFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a filter for commits. Can be used like a predicate
 *
 * @author m.kaspera, 17.05.2019
 */
public class CommitFilterImpl implements ICommitFilter
{

  private IBranch branch = null;
  private List<File> fileList = new ArrayList<>();
  private String author = null;
  private Instant startDate = null;
  private Instant endDate = null;

  @Override
  public CommitFilterImpl setBranch(IBranch pBranch)
  {
    branch = pBranch;
    return this;
  }

  @Override
  public CommitFilterImpl setFileList(List<File> pFileList)
  {
    fileList = pFileList;
    return this;
  }

  @Override
  public CommitFilterImpl setAuthor(String pAuthor)
  {
    author = pAuthor;
    return this;
  }

  @Override
  public CommitFilterImpl setStartDate(Instant pStartDate)
  {
    startDate = pStartDate;
    return this;
  }

  @Override
  public CommitFilterImpl setEndDate(Instant pEndDate)
  {
    endDate = pEndDate;
    return this;
  }

  @Override
  public @Nullable IBranch getBranch()
  {
    return branch;
  }

  @Override
  public @NotNull List<File> getFiles()
  {
    return fileList;
  }

  @Override
  public @Nullable String getAuthor()
  {
    return author;
  }

  @Override
  public @Nullable Instant getStartDate()
  {
    return startDate;
  }

  @Override
  public @Nullable Instant getEndDate()
  {
    return endDate;
  }

  @Override
  public boolean test(ICommit pCommit)
  {
    try
    {
      return (author == null || author.equals(pCommit.getAuthor()))
          && (startDate == null || startDate.isBefore(pCommit.getTime()))
          && (endDate == null || endDate.isAfter(pCommit.getTime()));
    }
    catch (NullPointerException pNPE)
    {
      return false;
    }
  }
}
