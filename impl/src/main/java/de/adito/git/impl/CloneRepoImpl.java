package de.adito.git.impl;

import com.google.inject.Inject;
import de.adito.git.api.ICloneRepo;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.TrackedBranchStatusCache;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.IConfig;
import de.adito.git.api.data.TrackedBranchStatus;
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
import java.io.IOException;
import java.util.*;

/**
 * a Class for the clone wizard. There is a checkout option inside the wizard.
 * Therefore it needs a list of branches of the new repository.
 *
 * @author a.arnold, 09.01.2019
 */
public class CloneRepoImpl implements ICloneRepo
{
  private final ISshProvider sshProvider;
  private final CloneConfig cloneConfig;


  @Inject
  CloneRepoImpl(ISshProvider pSshProvider, IKeyStore pKeyStore)
  {
    sshProvider = pSshProvider;
    cloneConfig = new CloneConfig(pKeyStore);
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  public List<IBranch> getBranchesFromRemoteRepo(@NotNull String pUrl, @Nullable String pSshPath) throws AditoGitException
  {
    Collection<Ref> refs;
    LsRemoteCommand lsRemoteCommand = Git.lsRemoteRepository()
        .setHeads(true)
        .setTags(false)
        .setTransportConfigCallback(_getTransportConfigCallBack(pSshPath))
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
    refs.forEach(branch -> branchesList.add(new BranchImpl(branch, new CloneRepoTrackedBranchStatusCache())));
    branchesList.sort(Comparator.comparing(IBranch::getActualName));
    return branchesList;
  }

  @Override
  public void cloneProject(@Nullable IProgressHandle pProgressHandle, @NotNull String pLocalPath, @NotNull String pProjectName, @NotNull String pURL,
                           @Nullable String pBranchName, @Nullable String pTag, @Nullable String pRemote, String pSshPath) throws AditoGitException
  {
    File target = new File(pLocalPath, pProjectName);
    CloneCommand cloneCommand = Git.cloneRepository()
        .setURI(pURL)
        .setTransportConfigCallback(_getTransportConfigCallBack(pSshPath))
        .setDirectory(target);

    if (pProgressHandle != null)
      cloneCommand = cloneCommand.setProgressMonitor(new _ProgressMonitor(pProgressHandle));
    if (pRemote != null)
      cloneCommand = cloneCommand.setRemote(pRemote);
    if (pBranchName != null && pTag == null)
      cloneCommand.setBranch(pBranchName);

    try
    {
      // Execute clone
      cloneCommand.call();

      // Checkout Tag
      if (pTag != null)
      {
        try (Git git = Git.open(target))
        {
          git.checkout()
              .setName(pTag)
              .call();
        }
      }
    }
    catch (GitAPIException | IOException e)
    {
      throw new AditoGitException("Failed to clone / checkout project", e);
    }
  }

  public IConfig getConfig()
  {
    return cloneConfig;
  }

  private TransportConfigCallback _getTransportConfigCallBack(@Nullable String pSshKeyLocation)
  {
    cloneConfig.setSshKeyLocationForUrl(pSshKeyLocation, null);
    return sshProvider.getTransportConfigCallBack(cloneConfig);
  }

  private static class CloneRepoTrackedBranchStatusCache extends TrackedBranchStatusCache
  {
    @Override
    public @NotNull TrackedBranchStatus getTrackedBranchStatus(@NotNull IBranch pBranch)
    {
      return TrackedBranchStatus.NONE;
    }
  }

  private static class _ProgressMonitor implements ProgressMonitor
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
