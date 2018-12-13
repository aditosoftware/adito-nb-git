package de.adito.git.nbm.guice;

import org.openide.filesystems.FileObject;

/**
 * @author m.kaspera 12.10.2018
 */
public interface IRepositoryProviderFactory
{
  RepositoryProvider create(FileObject pRepositoryFolder);
}
