package de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.DiffPane;

/**
 * Contains the line number and the y Coordinate for the baseline of the line number when drawn on the panel
 *
 * @author m.kaspera, 10.01.2019
 */
class LineNumber
{

  private final String number;
  private final int yCoordinate;
  private final int xCoordinate;

  /**
   * @param pNumber      the actual number of the line
   * @param pYCoordinate y coordinate of the baseline of the number for the line
   * @param pXCoordinate x coordinate of the baseline of the number for the line
   */
  LineNumber(int pNumber, int pYCoordinate, int pXCoordinate)
  {
    number = String.valueOf(pNumber);
    yCoordinate = pYCoordinate;
    xCoordinate = pXCoordinate;
  }

  /**
   * @return the actual number of the line represented as String
   */
  String getNumber()
  {
    return number;
  }

  /**
   * @return y coordinate of the baseline of the number for the line
   */
  int getYCoordinate()
  {
    return yCoordinate;
  }

  /**
   * @return x coordinate of the baseline of the number for the line
   */
  int getXCoordinate()
  {
    return xCoordinate;
  }
}
