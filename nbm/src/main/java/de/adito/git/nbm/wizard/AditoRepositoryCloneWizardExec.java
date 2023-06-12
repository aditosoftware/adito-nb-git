package de.adito.git.nbm.wizard;

import com.google.common.base.Strings;
import de.adito.git.api.ICloneRepo;
import de.adito.git.api.data.IBranch;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.progress.AsyncProgressFacadeImpl;
import lombok.NonNull;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.util.Optional;

import static de.adito.git.nbm.wizard.AditoRepositoryCloneWizard.*;

/**
 * @author a.arnold, 09.01.2019
 */
class AditoRepositoryCloneWizardExec
{
  private AditoRepositoryCloneWizardExec()
  {
  }

  static FileObject instantiate(@NonNull ProgressHandle pHandle, WizardDescriptor pWizardDescriptor)
  {
    final ICloneRepo cloneRepo = IGitConstants.INJECTOR.getInstance(ICloneRepo.class);
    pHandle.start();
    String localPath = (String) pWizardDescriptor.getProperty(W_PROJECT_PATH);
    String projectName = (String) pWizardDescriptor.getProperty(W_PROJECT_NAME);
    String repoPath = (String) pWizardDescriptor.getProperty(W_REPOSITORY_PATH);
    String sshPath = Strings.emptyToNull(Optional.ofNullable(pWizardDescriptor.getProperty(W_SSH_PATH)).map(String.class::cast).orElse(""));
    IBranch branch = (IBranch) pWizardDescriptor.getProperty(W_BRANCH);
    try
    {
      cloneRepo.cloneProject(AsyncProgressFacadeImpl.wrapNBHandle(pHandle), localPath, projectName, repoPath,
                             branch != null ? branch.getName() : null, null, null, sshPath);
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
