package de.adito.git.impl;

import de.adito.git.api.IBareRepo;
import de.adito.git.api.exception.AditoGitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

/**
 * @author m.kaspera, 10.06.2020
 */
public class BareRepoImpl implements IBareRepo
{

  @Override
  public void init(File pProjectFolder) throws AditoGitException
  {
    try
    {
      Git.init().setDirectory(pProjectFolder).call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }


}
