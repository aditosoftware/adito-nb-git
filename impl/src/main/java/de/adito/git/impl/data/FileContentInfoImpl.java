package de.adito.git.impl.data;

import com.google.common.base.Suppliers;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.IFileContentInfo;
import de.adito.git.impl.Util;

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
    fileContent = Suppliers.memoize(() -> new String(bytes, encoding.get()));
  }

  public FileContentInfoImpl(Supplier<String> pFileContent, Supplier<Charset> pEncoding)
  {
    fileContent = pFileContent;
    encoding = pEncoding;
  }

  public Supplier<String> getFileContent()
  {
    return fileContent;
  }

  public Supplier<Charset> getEncoding()
  {
    return encoding;
  }
}
