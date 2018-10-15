package de.adito.git.api;

/**
 * @author m.kaspera 15.10.2018
 */
public interface IFileSystemObserver {

    void addListener(IFileSystemChangeListener changeListener);

    void removeListener(IFileSystemChangeListener toRemove);

}
