package de.adito.git.impl.data;

import org.eclipse.jgit.api.Git;

/**
 * Factory interface for Guice and the data package
 *
 * @author m.kaspera, 24.12.2018
 */
public interface IDataFactory
{

  ConfigImpl createConfig(Git pGit);

}
