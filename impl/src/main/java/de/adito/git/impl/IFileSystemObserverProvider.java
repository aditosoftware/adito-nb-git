package de.adito.git.impl;

import de.adito.git.api.*;
import de.adito.git.api.data.IRepositoryDescription;

/**
 * @author m.kaspera 15.10.2018
 */
public interface IFileSystemObserverProvider
{

  IFileSystemObserver getFileSystemObserver(IRepositoryDescription pRepositoryDescription, IIgnoreFacade pGitIgnoreFacade);
}
