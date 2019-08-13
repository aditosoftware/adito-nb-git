package de.adito.git.nbm;

import de.adito.git.api.IFileSystemUtil;
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

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author m.kaspera, 18.12.2018
 */
public class NBFileSystemUtilImpl implements IFileSystemUtil
{

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private final FileSystem memoryFS = FileUtil.createMemoryFileSystem();
  private final HashMap<String, Image> artificialIconMap = new HashMap<>();
  private Image defaultMissingIconImage = ImageUtilities.icon2Image(MissingIcon.get16x16());

  @Override
  public void openFile(String pAbsolutePath) throws AditoGitException
  {
    openFile(new File(pAbsolutePath));
  }

  @Override
  public void openFile(File pFile) throws AditoGitException
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

  @NotNull
  @Override
  public Image getIcon(@NotNull File pFile, boolean pIsOpened)
  {
    Image image = null;
    try
    {
      FileObject fileObject = FileUtil.toFileObject(pFile);
      if (fileObject == null)
        return artificialIconMap.computeIfAbsent(FilenameUtils.getExtension(pFile.getName()), this::_createArtificialIcon);
      DataObject dataObject = DataObject.find(fileObject);
      image = pIsOpened ? dataObject.getNodeDelegate().getOpenedIcon(BeanInfo.ICON_COLOR_16x16)
          : dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
    }
    catch (Exception pE)
    {
      // do nothing, image will be null and the Missing/NotFoundIcon will be returned
    }
    if (image != null)
      return image;
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
}
