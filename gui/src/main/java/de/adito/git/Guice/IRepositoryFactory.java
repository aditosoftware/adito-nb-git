package de.adito.git.Guice;

import de.adito.git.RepositoryImpl;

/**
 * Factory interface for Guice to construct the RepositoryImpl object with a String as
 * parameter in the constructor
 *
 * @author m.kaspera 27.09.2018
 */
public interface IRepositoryFactory {

    RepositoryImpl create(String repoPath);

}
