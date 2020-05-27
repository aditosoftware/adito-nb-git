package de.adito.git.impl.data;

import de.adito.git.api.data.IBlame;
import de.adito.git.api.data.ICommit;
import org.eclipse.jgit.blame.BlameResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Date;

/**
 * @author a.arnold, 22.01.2019
 */
public class BlameImpl implements IBlame
{

  private final BlameResult blame;

  public BlameImpl(BlameResult pBlame)
  {
    blame = pBlame;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int computeNext()
  {
    try
    {
      return blame.computeNext();
    }
    catch (IOException pE)
    {
      throw new RuntimeException("can't compute next blame", pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSourceLine(int pIndex)
  {
    return blame.getSourceLine(pIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nullable
  public String getSourceAuthor(int pIndex)
  {
    return blame.getSourceAuthor(pIndex).getName();
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public Date getTimeStamp(int pIndex)
  {
    return blame.getSourceAuthor(pIndex).getWhen();
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public ICommit getSourceCommit(int pIndex)
  {
    return new CommitImpl(blame.getSourceCommit(pIndex));
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public String getSourcePath(int pIndex)
  {
    return blame.getSourcePath(pIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getLineCount()
  {
    return blame.getResultContents().size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    return blame.getResultPath();
  }

}
