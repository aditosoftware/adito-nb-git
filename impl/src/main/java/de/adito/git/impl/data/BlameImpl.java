package de.adito.git.impl.data;

import de.adito.git.api.data.IBlame;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.Date;

/**
 * @author a.arnold, 22.01.2019
 */
public class BlameImpl implements IBlame
{

  private BlameResult blame;

  public BlameImpl(BlameResult pBlame)
  {
    blame = pBlame;
  }

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

  @Override
  public int getSourceLine(int pIndex)
  {
    return blame.getSourceLine(pIndex);
  }

  @Override
  @Nullable
  public String getSourceAuthor(int pIndex)
  {
    return blame.getSourceAuthor(pIndex).getName();
  }

  @NotNull
  @Override
  public Date getTimeStamp(int pIndex)
  {
    return blame.getSourceAuthor(pIndex).getWhen();
  }

  @NotNull
  @Override
  public RevCommit getSourceCommit(int pIndex)
  {
    return blame.getSourceCommit(pIndex);
  }

  @NotNull
  @Override
  public String getSourcePath(int pIndex)
  {
    return blame.getSourcePath(pIndex);
  }

  @Override
  public RawText getRawText()
  {
    return blame.getResultContents();
  }

  @Override
  @NotNull
  public int getLineCount()
  {
    return blame.getResultContents().size();
  }


  @Override
  public String toString()
  {
    return blame.getResultPath();
  }

  private String _getRelativPath(@NotNull File pFile, @NotNull Repository pRepository)
  {
    String base = pRepository.getDirectory().getParent();
    String path = pFile.getAbsolutePath();
    return new File(base).toURI().relativize(new File(path).toURI()).getPath();
  }
}
