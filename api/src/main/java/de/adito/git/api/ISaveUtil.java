package de.adito.git.api;

/**
 * Defines methods responsible for persisting the state of files
 *
 * @author m.kaspera, 30.07.2019
 */
public interface ISaveUtil
{

  /**
   * tries to save all files that are in an unsaved state
   */
  void saveUnsavedFiles();

}
