package de.adito.git.nbm.wizard;

import de.adito.git.api.ICloneRepo;
import de.adito.git.api.data.IBranch;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.progress.AsyncProgressFacadeImpl;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.filesystems.*;

import java.io.File;

/**
 * @author a.arnold, 09.01.2019
 */
class AditoRepositoryCloneWizardExec
{
  private AditoRepositoryCloneWizardExec()
  {
  }

  static FileObject instantiate(@NotNull ProgressHandle pHandle, @NotNull String pLocalPath, @NotNull String pProjectName,
                                @NotNull String pRepoPath, String pSshPath, char[] pSshKey, IBranch pBranch)
  {
    final ICloneRepo cloneRepo = IGitConstants.INJECTOR.getInstance(ICloneRepo.class);
    pHandle.start();
    try
    {
      cloneRepo.cloneProject(AsyncProgressFacadeImpl.wrapNBHandle(pHandle), pLocalPath, pProjectName, pRepoPath,
                             pBranch != null ? pBranch.getName() : null, null, null, pSshPath, pSshKey);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Failed to clone repository", e);
    }
    finally
    {
      pHandle.finish();
    }

    //delete the ssh key
    for (int i = 0; i < pSshKey.length; i++)
    {
      pSshKey[i] = 0;
    }

    return FileUtil.toFileObject(new File(pLocalPath, pProjectName));
  }

}
