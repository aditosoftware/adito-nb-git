package de.adito.git.api;

import de.adito.git.api.exception.AditoGitException;

import java.io.File;

/**
 * Contains git methods that do not require an active repository, such as init
 *
 * @author m.kaspera, 10.06.2020
 */
public interface IBareRepo
{

  /**
   * performs a "git init" on the given folder, creating a new git repository for that folder
   *
   * @param pProjectFolder Folder for which a git repo should be initialized
   * @throws AditoGitException If the repo cannot be initialized because the path for the folder is invalid, cannot be written to or similar causes
   */
  void init(File pProjectFolder) throws AditoGitException;

}
