package de.adito.git.nbm.metainfo;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.metainfo.deploy.IDeployMetaInfoProvider;
import de.adito.git.api.data.*;
import de.adito.git.nbm.repo.RepositoryCache;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.util.lookup.ServiceProvider;

import java.util.*;

/**
 * Git MetaInfo provider at deployment time
 *
 * @author s.seemann, 01.07.2021
 */
@ServiceProvider(path = "Projects/de-adito-project/Lookup", service = IDeployMetaInfoProvider.class)
public class GitDeployMetaInfoProvider implements IDeployMetaInfoProvider
{
  private Project project;

  @SuppressWarnings("unused") // ServiceProvider
  public GitDeployMetaInfoProvider()
  {
  }

  @SuppressWarnings("unused") // ServiceProvider
  public GitDeployMetaInfoProvider(Project pProject)
  {
    project = pProject;
  }

  @NotNull
  @Override
  public String getName()
  {
    return "GitDeployMetaInfoProvider";
  }

  @NotNull
  @Override
  public Map<String, String> getMetaInfo()
  {
    return RepositoryCache.getInstance().findRepository(project).blockingFirst()
        .flatMap(pRep -> pRep.getRepositoryState().blockingFirst()
            .map(IRepositoryState::getCurrentBranch))
        .map(pBranch -> {
          Map<String, String> data = new HashMap<>();
          data.put("branchID", pBranch.getId());
          data.put("branchName", pBranch.getName());
          data.put("branchActualName", pBranch.getActualName());
          return data;
        })
        .orElse(Map.of());
  }
}
