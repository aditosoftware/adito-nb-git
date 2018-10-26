package de.adito.git.nbm.Guice;

import de.adito.git.api.data.IRepositoryDescription;

/**
 * @author m.kaspera 12.10.2018
 */
public interface IRepositoryProviderFactory {

    IRepositoryProvider create(IRepositoryDescription repositoryDescription);

}
