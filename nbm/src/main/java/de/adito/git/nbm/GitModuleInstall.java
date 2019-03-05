package de.adito.git.nbm;

import de.adito.git.api.IDiscardable;
import de.adito.git.nbm.repo.RepositoryCache;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.openide.modules.ModuleInstall;
import org.openide.util.lookup.Lookups;

/**
 * Start and close the Git Module at start and close of NetBeans
 *
 * @author a.arnold, 22.10.2018
 */
public class GitModuleInstall extends ModuleInstall
{

  /**
   * start the Git Module
   */
  @Override
  public void restored()
  {
    if (RepositoryCache.getInstance() != null)
      RepositoryCache.getInstance().init();
  }

  /**
   * close the Git Module
   */
  @Override
  public void uninstalled()
  {
    if (RepositoryCache.getInstance() != null)
    {
      RepositoryCache.getInstance().clear();

      // Shutdown GITVCS
      Lookups.forPath("Services/VersioningSystem")
          .lookupAll(VCSSystemProvider.VersioningSystem.class).stream()
          .map(VCSSystemProvider.VersioningSystem::getDelegate)
          .filter(IDiscardable.class::isInstance)
          .map(IDiscardable.class::cast)
          .forEach(IDiscardable::discard);
    }
  }
}
