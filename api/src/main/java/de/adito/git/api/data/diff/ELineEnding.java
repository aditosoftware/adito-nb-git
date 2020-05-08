package de.adito.git.api.data.diff;

/**
 * Represents a the kind of line ending used
 *
 * @author m.kaspera, 20.02.2020
 */
public enum ELineEnding
{

  WINDOWS("\r\n"),
  UNIX("\n"),
  MAC("\r");

  private final String lineEnding;

  ELineEnding(String pLineEnding)
  {
    lineEnding = pLineEnding;
  }

  /**
   * @return the used lineEnding for the type
   */
  public String getLineEnding()
  {
    return lineEnding;
  }

  /**
   * transforms the lineEnding given as string to one present in this enum, returns UNIX if none matches
   *
   * @param pLineEnding LineEnding as string
   * @return ELineEnding matching one of the lineEndings of this enum, or UNIX as default
   */
  public static ELineEnding getLineEnding(String pLineEnding)
  {
    if (pLineEnding.equals(WINDOWS.getLineEnding()))
      return WINDOWS;
    else if (pLineEnding.equals(MAC.getLineEnding()))
      return MAC;
    return UNIX;
  }
}
