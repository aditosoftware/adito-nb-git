package de.adito.git.nbm;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.adito.git.api.IFileSystemChangeListener;
import de.adito.git.api.IFileSystemObserver;
import de.adito.git.api.IIgnoreFacade;
import de.adito.git.api.data.IRepositoryDescription;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An observer class for the files in the version control version
 *
 * @author m.kaspera 15.10.2018
 */
class FileSystemObserverImpl implements IFileSystemObserver
{

  private final ArrayList<IFileSystemChangeListener> fileSystemChangeListeners = new ArrayList<>();
  private final FileSystemListener fsListener;
  private final FileObject root;
  private final IIgnoreFacade gitIgnoreFacade;
  private final CompositeDisposable disposable = new CompositeDisposable();
  private final EventBusListener eventBusListener = new EventBusListener(List.of("PLUGIN_NODEJS_MODULE_CHANGE", "DESIGNER_TRANSPILER_FINISHED"));

  /**
   * @param pRepositoryDescription IRepositoryDescription that contains the path to the project
   * @param pGitIgnoreFacade       IIgnoreFacade that can tell if a file is on the git ignore list
   */
  @SuppressWarnings("UnstableApiUsage")
  public FileSystemObserverImpl(@NotNull IRepositoryDescription pRepositoryDescription, @NotNull IIgnoreFacade pGitIgnoreFacade)
  {
    root = FileUtil.toFileObject(new File(pRepositoryDescription.getPath()));
    gitIgnoreFacade = pGitIgnoreFacade;
    if (root != null)
    {
      File gitFolder = new File(pRepositoryDescription.getPath(), ".git");
      fsListener = new FileSystemListener();

      /*
        The listener is registered in two locations:
          - Once on the git folder, this way all actions and file changes that change the git directory are registered (e.g. swapping branches, committing, adding)
          - Once globally via the FileUtil.addFileChangeListener. This way, changes to all opened file objects are recognised
        This means that the FileSystemObserver will not recognise changes that are made outside of git and Netbeans (e.g. via Notepad or other editors), however
        in order to recognise these changes, a recursive listener would have to be used. Since the recursive listeners open and hold all fileObjects under the path they
        are given, the tradeoff in memory consumption is not worth it (e.g. from 800MB to 1600MB in a modularized basic project). The current way should recognise all
        file changes in a normal workflow, a manual refresh (via the action in "local changes") is only necessary if the files are changed externally.
       */
      FileUtil.addRecursiveListener(fsListener, gitFolder, pathname -> !pathname.getName().equals("objects"), () -> false);
      addFSListener();

      /*
       Add listeners for certain EventBus events to get notifications when nodejs and transpiler events finish, since these do change files in the project
       */
      EventBus eventBus = Lookup.getDefault().lookup(EventBus.class);
      if (eventBus != null)
      {
        eventBus.register(eventBusListener);
        disposable.add(Disposable.fromRunnable(() -> eventBus.unregister(eventBusListener)));
      }

      disposable.add(Disposable.fromRunnable(() -> FileUtil.removeRecursiveListener(fsListener, gitFolder)));
      disposable.add(Disposable.fromRunnable(this::removeFSListener));
    }
    else
    {
      fsListener = null;
      System.err.println(NbBundle.getMessage(FileSystemObserverImpl.class, "Invalid.RecursiveListener"));
    }
  }

  @Override
  public void addListener(@NotNull IFileSystemChangeListener pChangeListener)
  {
    synchronized (fileSystemChangeListeners)
    {
      fileSystemChangeListeners.add(pChangeListener);
    }
  }

  @Override
  public void removeListener(@NotNull IFileSystemChangeListener pToRemove)
  {
    synchronized (fileSystemChangeListeners)
    {
      fileSystemChangeListeners.remove(pToRemove);
    }
  }

  @Override
  public void fireChange()
  {
    notifyListeners(null);
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

  /**
   * Adds the FileChangeListener to the Netbeans FileSystem so all changes to FileObjects are registered by it
   */
  private void addFSListener()
  {
    FileUtil.addFileChangeListener(fsListener);
  }

  /**
   * Remove active FileChangeListener from the Netbeans FileSystems
   */
  private void removeFSListener()
  {
    FileUtil.removeFileChangeListener(fsListener);
  }

  /**
   * Notifies, that the file system has changed
   *
   * @param pFileObject file that has changed, null to force-trigger an event
   */
  private void notifyListeners(@Nullable FileObject pFileObject)
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

  /**
   * FileChangeListener that delegates all file change events to the notifyListeners method
   */
  private class FileSystemListener implements FileChangeListener
  {
    @Override
    public void fileFolderCreated(FileEvent pEvent)
    {
      notifyListeners(pEvent == null ? null : pEvent.getFile());
    }

    @Override
    public void fileDataCreated(FileEvent pEvent)
    {
      notifyListeners(pEvent == null ? null : pEvent.getFile());
    }

    @Override
    public void fileChanged(FileEvent pEvent)
    {
      notifyListeners(pEvent == null ? null : pEvent.getFile());
    }

    @Override
    public void fileDeleted(FileEvent pEvent)
    {
      notifyListeners(pEvent == null ? null : pEvent.getFile());
    }

    @Override
    public void fileRenamed(FileRenameEvent pEvent)
    {
      notifyListeners(pEvent == null ? null : pEvent.getFile());
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent pEvent)
    {
      notifyListeners(pEvent == null ? null : pEvent.getFile());
    }
  }

  /**
   * Listener for the EventBus events from the designer
   */
  private class EventBusListener
  {

    private final List<Object> values;

    /**
     * @param pValues these are the values that the listener checks the bus for
     */
    public EventBusListener(List<Object> pValues)
    {
      values = pValues;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void fireEvent(@Nullable Object pObject)
    {
      if (pObject != null && values.contains(pObject))
        notifyListeners(null);
    }
  }
}
