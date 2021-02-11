package de.adito.git.nbm.designer;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.git.IGitVersioningSupport;
import de.adito.git.api.ICloneRepo;
import de.adito.git.nbm.IGitConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openide.util.lookup.ServiceProvider;

import java.io.File;
import java.util.Map;

/**
 * @author w.glanzer, 20.03.2019
 */
@ServiceProvider(service = IGitVersioningSupport.class)
public class GitVersioningSupportImpl implements IGitVersioningSupport
{

  @Override
  public boolean performClone(@NotNull String pRemoteURI, @NotNull File pTarget, @Nullable Map<String, String> pOptions) throws Exception
  {
    ICloneRepo repo = IGitConstants.INJECTOR.getInstance(ICloneRepo.class);
    String branchName = pOptions == null ? null : pOptions.get("branch");
    String remote = pOptions == null ? null : pOptions.get("remote");
    String tag = pOptions == null ? null : pOptions.get("tag");
    repo.cloneProject(null, pTarget.getParentFile().getAbsolutePath(), pTarget.getName(), pRemoteURI, branchName, tag, remote, null);
    return true;
  }

}
