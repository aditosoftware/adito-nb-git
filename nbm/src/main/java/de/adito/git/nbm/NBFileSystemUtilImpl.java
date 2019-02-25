package de.adito.git.nbm;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.exception.AditoGitException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.nio.charset.Charset;

/**
 * @author m.kaspera, 18.12.2018
 */
public class NBFileSystemUtilImpl implements IFileSystemUtil
{
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

  @Nullable
  @Override
  public Image getIcon(File pFile, boolean pIsOpened)
  {
    try
    {
      FileObject fileObject = FileUtil.toFileObject(pFile);
      if (fileObject == null)
        return null;
      DataObject dataObject = DataObject.find(fileObject);
      return pIsOpened ? dataObject.getNodeDelegate().getOpenedIcon(BeanInfo.ICON_COLOR_16x16)
          : dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
    }
    catch (Exception pE)
    {
      return null;
    }
  }

  @NotNull
  @Override
  public Charset getEncoding(@NotNull File pFile)
  {
    return FileEncodingQuery.getEncoding(FileUtil.toFileObject(pFile));
  }
}
