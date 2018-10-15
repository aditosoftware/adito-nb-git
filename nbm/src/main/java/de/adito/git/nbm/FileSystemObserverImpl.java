package de.adito.git.nbm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IFileSystemChangeListener;
import de.adito.git.api.IFileSystemObserver;
import de.adito.git.api.data.IRepositoryDescription;
import org.openide.filesystems.*;

import java.io.File;
import java.util.ArrayList;

/**
 * @author m.kaspera 15.10.2018
 */
public class FileSystemObserverImpl implements IFileSystemObserver {

    private ArrayList<IFileSystemChangeListener> fileSystemChangeListeners = new ArrayList<>();

    @Inject
    public FileSystemObserverImpl(@Assisted IRepositoryDescription pRepositoryDescription) {
        FileUtil.addRecursiveListener(new _FileSystemListener(), new File(pRepositoryDescription.getPath()).getParentFile());
    }

    @Override
    public void addListener(IFileSystemChangeListener changeListener) {
        fileSystemChangeListeners.add(changeListener);
    }

    @Override
    public void removeListener(IFileSystemChangeListener toRemove) {
        fileSystemChangeListeners.remove(toRemove);
    }

    private void _notifyListeners(){
        for(IFileSystemChangeListener fileSystemChangeListener: fileSystemChangeListeners){
            fileSystemChangeListener.fileSystemChange();
        }
    }

    private class _FileSystemListener implements  FileChangeListener{
        @Override
        public void fileFolderCreated(FileEvent fe) {
            _notifyListeners();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            _notifyListeners();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            _notifyListeners();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            _notifyListeners();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            _notifyListeners();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            _notifyListeners();
        }
    }
}
