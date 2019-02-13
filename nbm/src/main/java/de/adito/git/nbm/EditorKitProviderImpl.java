package de.adito.git.nbm;

import de.adito.git.gui.IEditorKitProvider;
import io.reactivex.annotations.NonNull;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.openide.text.CloneableEditorSupport;

import javax.swing.text.EditorKit;
import java.io.*;
import java.nio.file.Files;

/**
 * @author m.kaspera 13.11.2018
 */
public class EditorKitProviderImpl implements IEditorKitProvider
{
  @NonNull
  @Override
  public EditorKit getEditorKit(@NotNull String pFileDirectory)
  {
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

  @Override
  public EditorKit getEditorKitForContentType(@NotNull String pContentType)
  {
    if ("application/javascript".equals(pContentType))
      pContentType = "text/javascript";

    return CloneableEditorSupport.getEditorKit(pContentType);
  }
}
