package de.adito.git.nbm.vcs;

import de.adito.git.api.data.IFileChangeType;
import org.netbeans.modules.versioning.spi.VCSInterceptor;

import java.io.File;
import java.util.function.Consumer;

/**
 * @author a.arnold, 12.12.2018
 */
public class GitInterceptor extends VCSInterceptor
{
  private final Consumer<File> changedFilesConsumer;

  GitInterceptor(Consumer<File> pChangedFilesConsumer)
  {
    changedFilesConsumer = pChangedFilesConsumer;
  }

  @Override
  public void afterDelete(File pFile)
  {
    changedFilesConsumer.accept(pFile);
  }

  @Override
  public void afterMove(File pFrom, File pTo)
  {
    changedFilesConsumer.accept(pTo);
  }

  @Override
  public void afterCopy(File pFrom, File pTo)
  {
    changedFilesConsumer.accept(pTo);
  }

  @Override
  public void afterCreate(File pFile)
  {
    changedFilesConsumer.accept(pFile);
  }

  @Override
  public void afterChange(File pFile)
  {
    IFileChangeType change = GitVCSUtility.findChanges(pFile);
    if (change != null)
      changedFilesConsumer.accept(pFile);
  }
}
