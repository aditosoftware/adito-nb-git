package de.adito.git.nbm;

import de.adito.git.gui.IEditorKitProvider;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;

import javax.swing.text.EditorKit;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;

/**
 * @author m.kaspera 13.11.2018
 */
public class EditorKitProviderImpl implements IEditorKitProvider
{
  private static final Method createEditorKit_METHOD;

  static
  {
    try
    {
      createEditorKit_METHOD = CloneableEditorSupport.class.getDeclaredMethod("createEditorKit");
      createEditorKit_METHOD.setAccessible(true);
    }
    catch (NoSuchMethodException e)
    {
      // Nope, nothing to do here ... something is broken
      throw new RuntimeException(e);
    }
  }

  @NotNull
  @Override
  public EditorKit getEditorKit(@NotNull String pFileDirectory)
  {
    try
    {
      File file = new File(pFileDirectory).toPath().toAbsolutePath().toFile();
      FileObject fileObject = FileUtil.toFileObject(file);
      if (fileObject != null)
      {
        DataObject dataObject = DataObject.find(fileObject);
        if (dataObject != null)
        {
          CloneableEditorSupport ces = dataObject.getLookup().lookup(CloneableEditorSupport.class);
          if (ces != null)
          {
            EditorKit kit = (EditorKit) createEditorKit_METHOD.invoke(ces);
            if (kit != null)
              return kit;
          }
        }
      }
    }
    catch (Exception e)
    {
      // Nothing, just try another way
    }

    String mimeType = null;

    try
    {
      mimeType = Files.probeContentType(new File(pFileDirectory).toPath());
    }
    catch (IOException e)
    {
      // nothing to catch - ignore
      // if there is no content type the return value is "text/plain"
    }

    if (mimeType == null)
      mimeType = new Tika().detect(pFileDirectory);

    return getEditorKitForContentType(mimeType);
  }

  @NotNull
  @Override
  public EditorKit getEditorKitForContentType(@NotNull String pContentType)
  {
    return CloneableEditorSupport.getEditorKit(pContentType);
  }
}
