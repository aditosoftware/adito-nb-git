package de.adito.git.api.data.diff;

import java.nio.charset.Charset;
import java.util.function.Supplier;

/**
 * Stores information about a String and its encoding
 *
 * @author m.kaspera, 06.05.2019
 */
public interface IFileContentInfo
{

  /**
   * @return Supplier of a String with the contents of a file
   */
  Supplier<String> getFileContent();

  /**
   * @return Supplier of the used line endings for the text contents of the file
   */
  Supplier<ELineEnding> getLineEnding();

  /**
   * @return Supplier of the charset used to create the String from a byte array and vice versa
   */
  Supplier<Charset> getEncoding();

}
