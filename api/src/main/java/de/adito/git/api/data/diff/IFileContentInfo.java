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
   * Gets the content of the file. Should use a cache of some kind to not always read from the file
   *
   * @return Supplier of a String with the contents of a file
   */
  Supplier<String> getFileContent();

  /**
   * Get the lineendings used in the file. Should use a cache of some kind to not always read from the file
   *
   * @return Supplier of the used line endings for the text contents of the file
   */
  Supplier<ELineEnding> getLineEnding();

  /**
   * Get the encoding of the file. Should use a cache of some kind to not always read from the file
   *
   * @return Supplier of the charset used to create the String from a byte array and vice versa
   */
  Supplier<Charset> getEncoding();

}
