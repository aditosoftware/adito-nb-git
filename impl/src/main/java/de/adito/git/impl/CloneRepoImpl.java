package de.adito.git.impl;

import com.google.inject.Inject;
import de.adito.git.api.ICloneRepo;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.impl.data.BranchImpl;
import de.adito.git.impl.data.CloneConfig;
import de.adito.git.impl.ssh.ISshProvider;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * a Class for the clone wizard. There is a checkout option inside the wizard.
 * Therefore it needs a list of branches of the new repository.
 *
 * @author a.arnold, 09.01.2019
 */
public class CloneRepoImpl implements ICloneRepo
{
  private final ISshProvider sshProvider;


  @Inject
  CloneRepoImpl(ISshProvider pSshProvider)
  {
    sshProvider = pSshProvider;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  public List<IBranch> getBranchesFromRemoteRepo(@NotNull String pUrl, String pSshPath, char[] pSshKey) throws AditoGitException
  {
    Collection<Ref> refs;
    LsRemoteCommand lsRemoteCommand = Git.lsRemoteRepository()
        .setHeads(true)
        .setTags(false)
        .setTransportConfigCallback(_getTransportConfigCallBack(pSshPath, pSshKey))
        .setRemote(pUrl);
    try
    {
      refs = lsRemoteCommand.call();
    }
    catch (TransportException pAuthEx)
    {
      throw new AditoGitException("Authentication failed, most likely wrong password or invalid ssh key", pAuthEx);
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException("Can't get branches from Remote Repository", pE);
    }
    List<IBranch> branchesList = new ArrayList<>();
    refs.forEach(branch -> branchesList.add(new BranchImpl(branch)));
    return branchesList;
  }

  @Override
  public void cloneProject(@Nullable IProgressHandle pProgressHandle, @NotNull String pLocalPath, @NotNull String pProjectName,
                           @NotNull String pURL, @Nullable String pBranchName, @Nullable String pRemote,
                           String pSshPath, char[] pSshKey) throws AditoGitException
  {
    CloneCommand cloneCommand = Git.cloneRepository()
        .setURI(pURL)
        .setTransportConfigCallback(_getTransportConfigCallBack(pSshPath, pSshKey))
        .setDirectory(new File(pLocalPath, pProjectName));

    if (pProgressHandle != null)
      cloneCommand = cloneCommand.setProgressMonitor(new _ProgressMonitor(pProgressHandle));
    if (pRemote != null)
      cloneCommand = cloneCommand.setRemote(pRemote);

    if (pBranchName != null)
    {
      cloneCommand.setBranch(pBranchName);
    }
    try
    {
      cloneCommand.call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException("Failed to clone project", pE);
    }
  }

  private TransportConfigCallback _getTransportConfigCallBack(String pSshKeyLocation, char[] pSshKey)
  {
    CloneConfig cloneConfig = new CloneConfig();
    cloneConfig.setSshKeyLocation(pSshKeyLocation);
    cloneConfig.setPassword(pSshKey);

    return sshProvider.getTransportConfigCallBack(cloneConfig);
  }

  private class _ProgressMonitor implements ProgressMonitor
  {
    private final IProgressHandle progressHandle;
    private int counter;

    _ProgressMonitor(IProgressHandle pProgressHandle)
    {
      progressHandle = pProgressHandle;
    }

    @Override
    public void start(int pTotalTasks)
    {
      counter = 0;
    }

    @Override
    public void beginTask(String pTitle, int pTotalWork)
    {
      counter = 0;
      progressHandle.setDescription(pTitle);
      progressHandle.switchToDeterminate(pTotalWork);
    }

    @Override
    public void update(int pCompleted)
    {
      counter += pCompleted;
      progressHandle.progress(counter);
    }

    @Override
    public void endTask()
    {
      counter = 0;
    }

    @Override
    public boolean isCancelled()
    {
      return false;
    }
  }
}
