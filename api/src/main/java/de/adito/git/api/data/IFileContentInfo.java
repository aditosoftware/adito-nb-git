package de.adito.git.api.data;

import java.nio.charset.Charset;

/**
 * Stores information about a String and its encoding
 *
 * @author m.kaspera, 06.05.2019
 */
public interface IFileContentInfo
{

  /**
   * @return String with the contents of a file
   */
  String getFileContent();

  /**
   * @return Charset used to create the String from a byte array and vice versa
   */
  Charset getEncoding();

}
