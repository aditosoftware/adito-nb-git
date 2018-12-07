package de.adito.git.nbm.guice;

import com.google.inject.*;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.gui.guice.IRepositoryFactory;

import java.util.*;

/**
 * @author m.kaspera 26.10.2018
 */
@Singleton
class RepositoryProviderFactory implements IRepositoryProviderFactory
{

  private Map<IRepositoryDescription, RepositoryProvider> cache = new WeakHashMap<>();
  private IRepositoryFactory pFactory;

  @Inject
  public RepositoryProviderFactory(IRepositoryFactory pFactory)
  {
    this.pFactory = pFactory;
  }

  @Override
  public IRepositoryProvider create(IRepositoryDescription pRepositoryDescription)
  {
    return cache.computeIfAbsent(pRepositoryDescription, pDescr -> new RepositoryProvider(pFactory, pRepositoryDescription));
  }

}
