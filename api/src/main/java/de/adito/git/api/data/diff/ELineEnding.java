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
}
