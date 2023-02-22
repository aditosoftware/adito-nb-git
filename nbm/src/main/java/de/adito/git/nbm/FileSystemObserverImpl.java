package de.adito.git.nbm;

import de.adito.git.api.IFileSystemChangeListener;
import de.adito.git.api.IFileSystemObserver;
import de.adito.git.api.IIgnoreFacade;
import de.adito.git.api.data.IRepositoryDescription;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
      File gitFolder = new File(pRepositoryDescription.getPath(), ".git");
      fsListener = new _FileSystemListener();

      /*
        The listener is registered in two locations:
          - Once on the git folder, this way all actions and file changes that change the git directory are registered (e.g. swapping branches, committing, adding)
          - Once globally via the FileUtil.addFileChangeListener. This way, changes to all opened file objects are recognised
        This means that the FileSystemObserver will not recognise changes that are made outside of git and Netbeans (e.g. via Notepad or other editors), however
        in order to recognise these changes, a recursive listener would have to be used. Since the recursive listeners open and hold all fileObjects under the path they
        are given, the tradeoff in memory consumption is not worth it (e.g. from 800MB to 1600MB in a modularized basic project). The current way should recognise all
        file changes in a normal workflow, a manual refresh (via the action in "local changes") is only necessary if the files are changed externally.
       */
      FileUtil.addFileChangeListener(fsListener, gitFolder);
      _addFSListener();

      disposable.add(Disposable.fromRunnable(() -> FileUtil.removeFileChangeListener(fsListener, gitFolder)));
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

    if (!disposable.isDisposed())
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
