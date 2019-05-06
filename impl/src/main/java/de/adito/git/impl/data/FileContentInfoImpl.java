package de.adito.git.impl.data;

import de.adito.git.api.data.IFileContentInfo;

import java.nio.charset.Charset;

/**
 * Class used to store information about a String and the encoding used to transform it into a byte array
 *
 * @author m.kaspera, 06.05.2019
 */
public class FileContentInfoImpl implements IFileContentInfo
{

  private final String fileContent;
  private final Charset encoding;

  public FileContentInfoImpl(String pFileContent, Charset pEncoding)
  {
    fileContent = pFileContent;
    encoding = pEncoding;
  }

  public String getFileContent()
  {
    return fileContent;
  }

  public Charset getEncoding()
  {
    return encoding;
  }
}
