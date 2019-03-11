package de.adito.git.nbm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IFileSystemChangeListener;
import de.adito.git.api.IFileSystemObserver;
import de.adito.git.api.data.IRepositoryDescription;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;

import java.io.File;
import java.util.ArrayList;

/**
 * An observer class for the files in the version control version
 *
 * @author m.kaspera 15.10.2018
 */
public class FileSystemObserverImpl implements IFileSystemObserver
{

  private final ArrayList<IFileSystemChangeListener> fileSystemChangeListeners = new ArrayList<>();

  @Inject
  public FileSystemObserverImpl(@Assisted IRepositoryDescription pRepositoryDescription)
  {
    FileObject fileObject = FileUtil.toFileObject(new File(pRepositoryDescription.getPath()));
    if (fileObject != null)
    {
      fileObject.addRecursiveListener(new _FileSystemListener());
    }
    else
    {
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
    _notifyListeners();
  }

  private void _notifyListeners()
  {
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
      _notifyListeners();
    }

    @Override
    public void fileDataCreated(FileEvent pEvent)
    {
      _notifyListeners();
    }

    @Override
    public void fileChanged(FileEvent pEvent)
    {
      _notifyListeners();
    }

    @Override
    public void fileDeleted(FileEvent pEvent)
    {
      _notifyListeners();
    }

    @Override
    public void fileRenamed(FileRenameEvent pEvent)
    {
      _notifyListeners();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent pEvent)
    {
      _notifyListeners();
    }
  }
}
