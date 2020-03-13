package de.adito.git.nbm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.icon.MissingIcon;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import javax.inject.Singleton;
import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author m.kaspera, 18.12.2018
 */
public class NBFileSystemUtilImpl implements IFileSystemUtil
{

  private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
  private static final Object FILE_OBJECT_LOCK = new Object();
  private static final ThreadPoolExecutor EXECUTOR_SERVICE = new ThreadPoolExecutor(0, NUM_CORES, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private final FileSystem memoryFS = FileUtil.createMemoryFileSystem();
  private final HashMap<String, Image> artificialIconMap = new HashMap<>();
  private final LoadingCache<_IconKey, Image> iconCache;
  private Image defaultMissingIconImage = ImageUtilities.icon2Image(MissingIcon.get16x16());

  @Singleton
  public NBFileSystemUtilImpl()
  {
    iconCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterAccess(30, TimeUnit.SECONDS).build(new CacheLoader<>()
    {
      @Override
      public Image load(@NotNull _IconKey pIconKey)
      {
        return _loadImage(pIconKey.file, pIconKey.pIsOpened);
      }
    });
  }

  @Override
  public void openFile(@NotNull String pAbsolutePath) throws AditoGitException
  {
    openFile(new File(pAbsolutePath));
  }

  @Override
  public void openFile(@NotNull File pFile) throws AditoGitException
  {
    try
    {
      FileObject fileObject = FileUtil.toFileObject(pFile);
      if (fileObject != null)
      {
        DataObject.find(fileObject).getLookup().lookupAll(OpenCookie.class).forEach(Openable::open);
      }
      else
      {
        throw new AditoGitException(NbBundle.getMessage(NBFileSystemUtilImpl.class, "Invalid.FileObject", pFile.getAbsolutePath()));
      }
    }
    catch (DataObjectNotFoundException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  public void preLoadIcons(@NotNull List<IFileChangeType> pFiles)
  {
    if (pFiles.size() > 50)
    {
      _preLoadInParallel(pFiles);
    }
    else
    {
      _preLoadIcons(pFiles);
    }
  }

  @NotNull
  @Override
  public Image getIcon(@NotNull File pFile, boolean pIsOpened)
  {
    try
    {
      return iconCache.get(new _IconKey(pFile, pFile.lastModified(), pIsOpened));
    }
    catch (ExecutionException pE)
    {
      // return the default missing/error icon
    }
    return defaultMissingIconImage;
  }

  @NotNull
  @Override
  public Charset getEncoding(@NotNull File pFile)
  {
    try
    {
      return FileEncodingQuery.getEncoding(FileUtil.toFileObject(new File(pFile.getCanonicalPath())));
    }
    catch (IOException pE)
    {
      return FileEncodingQuery.getEncoding(FileUtil.toFileObject(pFile));
    }
  }

  @NotNull
  @Override
  public Charset getEncoding(@NotNull byte[] pContent)
  {
    FileObject tempFo = null;

    try
    {
      tempFo = memoryFS.getRoot().createData(UUID.randomUUID().toString());
      try (OutputStream out = tempFo.getOutputStream())
      {
        IOUtils.write(pContent, out);
      }
      return FileEncodingQuery.getEncoding(tempFo);
    }
    catch (Exception e)
    {
      logger.log(Level.SEVERE, e, () -> "Git: Error while determining encoding for byte array");
      return StandardCharsets.UTF_8;
    }
    finally
    {
      try
      {
        if (tempFo != null)
          tempFo.delete();
      }
      catch (Exception e)
      {
        //nothing
      }
    }
  }

  /**
   * Split the preload process into "number of cores" parts and let them run in a Threadpool of size "number of cores"
   *
   * @param pFileChangeTypes List of IFileChangeTypes for which the icons should be pre-loaded
   */
  private void _preLoadInParallel(@NotNull List<IFileChangeType> pFileChangeTypes)
  {
    int numberPerThread = pFileChangeTypes.size() / NUM_CORES;
    for (int index = 0; index < NUM_CORES; index++)
    {
      // if the index is the first index start at 0, otherwise add one to numberPerThread * index to not do the last item of the previous thread twice
      int startIndex = index == 0 ? 0 : numberPerThread * index + 1;
      int endIndex = index == NUM_CORES - 1 ? pFileChangeTypes.size() : numberPerThread * (index + 1);
      EXECUTOR_SERVICE.submit(() -> _preLoadIcons(pFileChangeTypes.subList(startIndex, endIndex)));
    }
  }

  /**
   * Pre-load icons for a list of IFileChangeTypes
   *
   * @param pFileChangeTypes List of IFileChangeTypes for which the icons should be pre-loaded
   */
  private void _preLoadIcons(@NotNull List<IFileChangeType> pFileChangeTypes)
  {
    for (IFileChangeType fileChangeType : pFileChangeTypes)
    {
      try
      {
        // on using false as parameter for isOpened: It is unclear whether the parameter will be true or false in the actual case, however it is more likely that
        // the parameter will be false (more leaves than open nodes). Additionally, the nodes are more likely to be the same, and thus more calls will already be cached
        // anyways
        iconCache.get(new _IconKey(fileChangeType.getFile(), Files.getLastModifiedTime(fileChangeType.getFile().toPath()).toMillis(), false));
      }
      catch (ExecutionException | IOException pE)
      {
        // do nothing, icon is not pre-loaded
      }
    }
  }

  private Image _loadImage(File pFile, boolean pIsOpened)
  {
    Image image = null;
    try
    {
      FileObject fileObject = FileUtil.toFileObject(pFile);
      if (fileObject == null)
        return artificialIconMap.computeIfAbsent(FilenameUtils.getExtension(pFile.getName()), this::_createArtificialIcon);
      synchronized (FILE_OBJECT_LOCK)
      {
        DataObject dataObject = DataObject.find(fileObject);
        image = pIsOpened ? dataObject.getNodeDelegate().getOpenedIcon(BeanInfo.ICON_COLOR_16x16)
            : dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
      }
    }
    catch (Exception pE)
    {
      // do nothing, image will be null and the Missing/NotFoundIcon will be returned
    }
    if (image != null)
      return image;
    return defaultMissingIconImage;
  }

  /**
   * @param pFileExtension extension of the file, used to determine the icon/image
   * @return Image representing the type of file
   */
  @NotNull
  private Image _createArtificialIcon(@NotNull String pFileExtension)
  {
    FileObject tempFo = null;
    Image image = null;
    try
    {
      tempFo = memoryFS.getRoot().createData(UUID.randomUUID().toString(), pFileExtension);
      DataObject dataObject = DataObject.find(tempFo);
      image = dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
    }
    catch (IOException pE)
    {
      logger.log(Level.SEVERE, pE, () -> "Git: Error while trying to find a matching icon for a file with extension " + pFileExtension);
    }
    finally
    {
      try
      {
        if (tempFo != null)
          tempFo.delete();
      }
      catch (Exception e)
      {
        //nothing
      }
    }
    if (image != null)
      return image;
    return defaultMissingIconImage;
  }

  /**
   * Key for the Hashmap
   */
  private static class _IconKey
  {
    final File file;
    final long lastModified;
    final boolean pIsOpened;

    _IconKey(@NotNull File pFile, long pLastModified, boolean pPIsOpened)
    {
      file = pFile;
      lastModified = pLastModified;
      pIsOpened = pPIsOpened;
    }

    @Override
    public boolean equals(Object pO)
    {
      if (this == pO) return true;
      if (pO == null || getClass() != pO.getClass()) return false;
      _IconKey iconKey = (_IconKey) pO;
      return lastModified == iconKey.lastModified &&
          pIsOpened == iconKey.pIsOpened &&
          file.equals(iconKey.file);
    }

    @Override
    public int hashCode()
    {
      return Objects.hash(file, lastModified, pIsOpened);
    }
  }
}
