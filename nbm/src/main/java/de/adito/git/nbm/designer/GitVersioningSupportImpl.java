package de.adito.git.nbm.designer;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.git.IGitVersioningSupport;
import de.adito.aditoweb.nbm.nbide.nbaditointerface.git.IRemoteBranch;
import de.adito.aditoweb.nbm.nbide.nbaditointerface.git.exceptions.AditoVersioningException;
import de.adito.git.api.ICloneRepo;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.nbm.IGitConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openide.util.lookup.ServiceProvider;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author w.glanzer, 20.03.2019
 */
@ServiceProvider(service = IGitVersioningSupport.class)
public class GitVersioningSupportImpl implements IGitVersioningSupport
{
  private static final ICloneRepo repo = IGitConstants.INJECTOR.getInstance(ICloneRepo.class);

  @Override
  public boolean performClone(@NotNull String pRemoteURI, @NotNull File pTarget, @Nullable Map<String, String> pOptions) throws Exception
  {
    String branchName = pOptions == null ? null : pOptions.get("branch");
    String remote = pOptions == null ? null : pOptions.get("remote");
    String tag = pOptions == null ? null : pOptions.get("tag");
    repo.cloneProject(null, pTarget.getParentFile().getAbsolutePath(), pTarget.getName(), pRemoteURI, branchName, tag, remote, null);
    return true;
  }

  @NotNull
  @Override
  public List<IRemoteBranch> getBranchesInRepository(@NotNull String pRemoteUrl) throws AditoVersioningException
  {
    try
    {
      return repo.getBranchesFromRemoteRepo(pRemoteUrl, null)
          .stream()
          .map(RemoteBranchImpl::new)
          .collect(Collectors.toList());
    }
    catch (AditoGitException pE)
    {
      throw new AditoVersioningException(pE);
    }
  }

  /**
   * Basic Implementation that just stores the name and id of the given branch
   */
  private static class RemoteBranchImpl implements IRemoteBranch
  {

    private final String name;
    private final String id;

    public RemoteBranchImpl(@NotNull IBranch pBranch)
    {
      name = pBranch.getSimpleName();
      id = pBranch.getId();
    }

    @Override
    public String getName()
    {
      return name;
    }

    @Override
    public String getId()
    {
      return id;
    }
  }

}
