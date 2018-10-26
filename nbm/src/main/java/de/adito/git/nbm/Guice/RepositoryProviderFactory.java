package de.adito.git.nbm.Guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.gui.guice.IRepositoryFactory;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author m.kaspera 26.10.2018
 */
@Singleton
class RepositoryProviderFactory implements IRepositoryProviderFactory {

    private Map<IRepositoryDescription, RepositoryProvider> cache = new WeakHashMap<>();
    private IRepositoryFactory pFactory;

    @Inject
    public RepositoryProviderFactory(IRepositoryFactory pFactory) {
        this.pFactory = pFactory;
    }

    @Override
    public IRepositoryProvider create(IRepositoryDescription repositoryDescription) {
        return cache.computeIfAbsent(repositoryDescription, pDescr -> new RepositoryProvider(pFactory, repositoryDescription));
    }

}
