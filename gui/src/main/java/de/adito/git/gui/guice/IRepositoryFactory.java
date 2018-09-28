package de.adito.git.gui.guice;

import de.adito.git.impl.RepositoryImpl;

/**
 * Factory interface for Guice to construct the RepositoryImpl object with a String as
 * parameter in the constructor
 *
 * @author m.kaspera 27.09.2018
 */
public interface IRepositoryFactory {

    RepositoryImpl create(String repoPath);

}
