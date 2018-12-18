package de.adito.git.nbm;

import de.adito.git.api.IFileOpener;
import de.adito.git.api.exception.AditoGitException;
import org.netbeans.api.actions.Openable;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import java.io.File;

/**
 * @author m.kaspera, 18.12.2018
 */
public class NBFileOpenerImpl implements IFileOpener
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
}
