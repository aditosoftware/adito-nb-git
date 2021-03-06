package de.adito.git.nbm.wizard;

import com.google.common.base.Strings;
import de.adito.git.api.ICloneRepo;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.IConfig;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.progress.AsyncProgressFacadeImpl;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;

import static de.adito.git.api.data.IConfig.REMOTE_SECTION_KEY;
import static de.adito.git.api.data.IConfig.SSH_KEY_KEY;
import static de.adito.git.nbm.wizard.AditoRepositoryCloneWizard.*;

/**
 * @author a.arnold, 09.01.2019
 */
class AditoRepositoryCloneWizardExec
{
  private AditoRepositoryCloneWizardExec()
  {
  }

  static FileObject instantiate(@NotNull ProgressHandle pHandle, WizardDescriptor pWizardDescriptor)
  {
    final ICloneRepo cloneRepo = IGitConstants.INJECTOR.getInstance(ICloneRepo.class);
    pHandle.start();
    String localPath = pWizardDescriptor.getProperty(W_PROJECT_PATH).toString();
    String projectName = pWizardDescriptor.getProperty(W_PROJECT_NAME).toString();
    String repoPath = pWizardDescriptor.getProperty(W_REPOSITORY_PATH).toString();
    String sshPath = Strings.emptyToNull(pWizardDescriptor.getProperty(W_SSH_PATH).toString());
    IBranch branch = (IBranch) pWizardDescriptor.getProperty(W_BRANCH);
    try
    {
      IConfig cloneConfig = cloneRepo.getConfig();
      cloneRepo.cloneProject(AsyncProgressFacadeImpl.wrapNBHandle(pHandle), localPath, projectName, repoPath,
                             branch != null ? branch.getName() : null, null, null, sshPath);
      String sshKeyLocation = cloneConfig.getSshKeyLocation(repoPath);
      if (sshKeyLocation != null)
      {
        try (Git git = new Git(FileRepositoryBuilder.create(new File(localPath + File.separator + ".git"))))
        {
          StoredConfig config = git.getRepository().getConfig();
          config.setString(REMOTE_SECTION_KEY, "origin", SSH_KEY_KEY, sshKeyLocation);
          config.save();
        }
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException("Failed to clone repository", e);
    }
    finally
    {
      pHandle.finish();
    }

    return FileUtil.toFileObject(new File(localPath, projectName));
  }

}
