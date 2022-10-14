package de.adito.git.nbm;

import de.adito.git.api.*;
import de.adito.git.api.data.IRepositoryDescription;
import io.reactivex.rxjava3.disposables.*;
import org.jetbrains.annotations.*;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;

import java.io.File;
import java.util.ArrayList;

/**
 * An observer class for the files in the version control version
 *
 * @author m.kaspera 15.10.2018
 */
class FileSystemObserverImpl implements IFileSystemObserver
{

  private final ArrayList<IFileSystemChangeListener> fileSystemChangeListeners = new ArrayList<>();
  private final _FileSystemListener fsListener;
  private final FileObject root;
  private final IIgnoreFacade gitIgnoreFacade;
  private final CompositeDisposable disposable = new CompositeDisposable();

  public FileSystemObserverImpl(IRepositoryDescription pRepositoryDescription, @NotNull IIgnoreFacade pGitIgnoreFacade)
  {
    root = FileUtil.toFileObject(new File(pRepositoryDescription.getPath()));
    gitIgnoreFacade = pGitIgnoreFacade;
    if (root != null)
    {
      fsListener = new _FileSystemListener();
      _addFSListener();
      disposable.add(Disposable.fromRunnable(this::_removeFSListener));
    }
    else
    {
      fsListener = null;
      System.err.println(NbBundle.getMessage(FileSystemObserverImpl.class, "Invalid.RecursiveListener"));
    }
  }

  @Override
  public void addListener(IFileSystemChangeListener pChangeListener)
  {
    synchronized (fileSystemChangeListeners)
    {
      fileSystemChangeListeners.add(pChangeListener);
    }
  }

  @Override
  public void removeListener(IFileSystemChangeListener pToRemove)
  {
    synchronized (fileSystemChangeListeners)
    {
      fileSystemChangeListeners.remove(pToRemove);
    }
  }

  @Override
  public void fireChange()
  {
    _notifyListeners(null);
  }

  @Override
  public void discard()
  {
    synchronized (fileSystemChangeListeners)
    {
      fileSystemChangeListeners.clear();
    }

    if(!disposable.isDisposed())
      disposable.dispose();
  }

  private void _addFSListener()
  {
    FileUtil.addFileChangeListener(fsListener);
  }

  private void _removeFSListener()
  {
    FileUtil.removeFileChangeListener(fsListener);
  }

  /**
   * Notifies, that the file system has changed
   *
   * @param pFileObject file that has changed, null to force-trigger an event
   */
  private void _notifyListeners(@Nullable FileObject pFileObject)
  {
    // Check if notification has to be scheduled
    // null triggers a notification every time, because we do not know which fileobject has changed
    if (pFileObject != null)
    {
      // check: is located under root?
      if (!FileUtil.isParentOf(root, pFileObject))
        return;

      // check: file ignored?
      if (gitIgnoreFacade.isIgnored(FileUtil.toFile(pFileObject)))
        return;
    }

    ArrayList<IFileSystemChangeListener> copy;

    synchronized (fileSystemChangeListeners)
    {
      copy = new ArrayList<>(fileSystemChangeListeners);
    }

    for (IFileSystemChangeListener fileSystemChangeListener : copy)
    {
      fileSystemChangeListener.fileSystemChange();
    }
  }

  private class _FileSystemListener implements FileChangeListener
  {
    @Override
    public void fileFolderCreated(FileEvent pEvent)
    {
      _notifyListeners(pEvent == null ? null : pEvent.getFile());
    }

    @Override
    public void fileDataCreated(FileEvent pEvent)
    {
      _notifyListeners(pEvent == null ? null : pEvent.getFile());
    }

    @Override
    public void fileChanged(FileEvent pEvent)
    {
      _notifyListeners(pEvent == null ? null : pEvent.getFile());
    }

    @Override
    public void fileDeleted(FileEvent pEvent)
    {
      _notifyListeners(pEvent == null ? null : pEvent.getFile());
    }

    @Override
    public void fileRenamed(FileRenameEvent pEvent)
    {
      _notifyListeners(pEvent == null ? null : pEvent.getFile());
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent pEvent)
    {
      _notifyListeners(pEvent == null ? null : pEvent.getFile());
    }
  }
}
