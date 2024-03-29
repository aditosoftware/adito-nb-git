package de.adito.git.impl.data.diff;

import com.google.common.base.Suppliers;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.diff.ELineEnding;
import de.adito.git.api.data.diff.IFileContentInfo;
import de.adito.git.impl.Util;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
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
  private final Supplier<String> originalFileContent;
  private final Supplier<Charset> encoding;
  private final Supplier<ELineEnding> lineEnding;

  public FileContentInfoImpl(Supplier<byte[]> pBytes, IFileSystemUtil pFileSystemUtil)
  {
    byte[] bytes = pBytes.get();
    encoding = Suppliers.memoize(() -> Util.getEncoding(bytes, pFileSystemUtil));
    originalFileContent = () -> new String(bytes, encoding.get());
    fileContent = Suppliers.memoize(() -> _cleanString(new String(bytes, encoding.get())));
    lineEnding = Suppliers.memoize(this::_findLineEnding);
  }

  public FileContentInfoImpl(Supplier<String> pFileContent, Supplier<Charset> pEncoding)
  {
    fileContent = Suppliers.memoize(() -> _cleanString(pFileContent.get()));
    originalFileContent = pFileContent;
    encoding = pEncoding;
    lineEnding = Suppliers.memoize(this::_findLineEnding);
  }

  public Supplier<String> getFileContent()
  {
    return fileContent;
  }

  @Override
  public Supplier<ELineEnding> getLineEnding()
  {
    return lineEnding;
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
  @NonNull
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

  /**
   * Returns the LineEnding, which is most often found in the fileContent.
   *
   * @return the LineEnding, which is most often found
   */
  private ELineEnding _findLineEnding()
  {
    String content = originalFileContent.get();

    int windows = StringUtils.countMatches(content, ELineEnding.WINDOWS.getLineEnding());

    // The windows line-endings are also found here, so they have to be subtracted.
    int unix = StringUtils.countMatches(content, ELineEnding.UNIX.getLineEnding()) - windows;
    int mac = StringUtils.countMatches(content, ELineEnding.MAC.getLineEnding()) - windows;

    int max = Math.max(windows, Math.max(unix, mac));

    if (max == windows)
      return ELineEnding.WINDOWS;
    else if (max == unix)
      return ELineEnding.UNIX;
    else
      return ELineEnding.MAC;
  }
}
