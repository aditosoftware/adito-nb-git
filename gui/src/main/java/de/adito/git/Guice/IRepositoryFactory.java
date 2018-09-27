package de.adito.git.Guice;

import de.adito.git.RepositoryImpl;

/**
 * @author m.kaspera 27.09.2018
 */
public interface IRepositoryFactory {

    RepositoryImpl create(String repoPath);

}
