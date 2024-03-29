package de.adito.git.impl;

import com.google.inject.Inject;
import de.adito.git.api.ICloneRepo;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.TrackedBranchStatusCache;
import de.adito.git.api.data.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.impl.data.BranchImpl;
import de.adito.git.impl.data.CloneConfig;
import de.adito.git.impl.data.TagImpl;
import de.adito.git.impl.ssh.ISshProvider;
import lombok.NonNull;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
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
  CloneRepoImpl(ISshProvider pSshProvider, IKeyStore pKeyStore, IPrefStore pPrefStore)
  {
    sshProvider = pSshProvider;
    cloneConfig = new CloneConfig(pKeyStore, pPrefStore);
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  public List<IBranch> getBranchesFromRemoteRepo(@NonNull String pUrl, @Nullable String pSshPath) throws AditoGitException
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
  public @NonNull List<ITag> getTagsFromRemoteRepo(@NonNull String pUrl, @Nullable String pSshPath) throws AditoGitException
  {
    Collection<Ref> refs;
    LsRemoteCommand lsRemoteCommand = Git.lsRemoteRepository()
        .setHeads(false)
        .setTags(true)
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
    List<ITag> tagList = new ArrayList<>();
    refs.forEach(branch -> tagList.add(new TagImpl(branch)));
    tagList.sort(Comparator.comparing(ITag::getName));
    return tagList;
  }

  @Override
  public void cloneProject(@Nullable IProgressHandle pProgressHandle, @NonNull String pLocalPath, @NonNull String pProjectName, @NonNull String pURL,
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
    public @NonNull TrackedBranchStatus getTrackedBranchStatus(@NonNull IBranch pBranch)
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
