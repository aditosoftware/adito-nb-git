package de.adito.git.api;

/**
 * Interface around an Observer of the file system
 *
 * @author m.kaspera 15.10.2018
 */
public interface IFileSystemObserver
{

  /**
   * registers the passed listener
   *
   * @param pChangeListener the IFileSystemChangeListener that should be registered and get notifications in the future
   */
  void addListener(IFileSystemChangeListener pChangeListener);

  /**
   * removes one of the listeners
   *
   * @param pToRemove the IFileSystemChangeListener to remove
   */
  void removeListener(IFileSystemChangeListener pToRemove);

  /**
   * triggers an update in all listeners
   */
  void fireChange();

}
