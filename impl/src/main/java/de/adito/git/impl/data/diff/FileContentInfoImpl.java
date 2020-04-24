package de.adito.git.impl.data.diff;

import com.google.common.base.Suppliers;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.diff.ELineEnding;
import de.adito.git.api.data.diff.IFileContentInfo;
import de.adito.git.impl.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.function.Supplier;

/**
 * Class used to store information about a String and the encoding used to transform it into a byte array
 *
 * @author m.kaspera, 06.05.2019
 */
public class FileContentInfoImpl implements IFileContentInfo
{

  private final Supplier<String> fileContent;
  private final Supplier<Charset> encoding;

  public FileContentInfoImpl(Supplier<byte[]> pBytes, IFileSystemUtil pFileSystemUtil)
  {
    byte[] bytes = pBytes.get();
    encoding = Suppliers.memoize(() -> Util.getEncoding(bytes, pFileSystemUtil));
    fileContent = Suppliers.memoize(() -> _cleanString(new String(bytes, encoding.get())));
  }

  public FileContentInfoImpl(Supplier<String> pFileContent, Supplier<Charset> pEncoding)
  {
    fileContent = Suppliers.memoize(() -> _cleanString(pFileContent.get()));
    encoding = pEncoding;
  }

  public Supplier<String> getFileContent()
  {
    return fileContent;
  }

  @Override
  public Supplier<ELineEnding> getLineEnding()
  {
    return null;
  }

  public Supplier<Charset> getEncoding()
  {
    return encoding;
  }

  /**
   * Make the newlines in the string uniform \n
   *
   * @param pUnCleanString String that should be cleaned such that the newlines are always only \n
   * @return String with only \n as newlines
   */
  @NotNull
  private static String _cleanString(@Nullable String pUnCleanString)
  {
    if (pUnCleanString != null)
    {
      if (pUnCleanString.contains("\n"))
        pUnCleanString = pUnCleanString.replace("\r", "");
      else
        pUnCleanString = pUnCleanString.replace("\r", "\n");
      return pUnCleanString;
    }
    return "";
  }
}
