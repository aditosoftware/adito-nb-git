package de.adito.git.nbm.Guice;

import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.gui.RepositoryProvider;

/**
 * @author m.kaspera 12.10.2018
 */
public interface IRepositoryProviderFactory {

    RepositoryProvider create(IRepositoryDescription repositoryDescription);

}
