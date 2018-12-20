package de.adito.git.nbm;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.exception.AditoGitException;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;

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
        throw new AditoGitException("Could not find FileObject for file " + pFile.getAbsolutePath());
      }
    }
    catch (DataObjectNotFoundException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  @NotNull
  @Override
  public Charset getEncoding(@NotNull File pFile)
  {
    return FileEncodingQuery.getEncoding(FileUtil.toFileObject(pFile));
  }
}
