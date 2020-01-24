package de.adito.git.gui.swing;

/**
 * Contains the line number and the y Coordinate for the baseline of the line number when drawn on the panel
 *
 * @author m.kaspera, 10.01.2019
 */
public class LineNumber
{

  private final String number;
  private final int yCoordinate;
  private final int xCoordinate;

  /**
   * @param pNumber      the actual number of the line
   * @param pXCoordinate x coordinate of the baseline of the number for the line
   * @param pYCoordinate y coordinate of the baseline of the number for the line
   */
  public LineNumber(int pNumber, int pXCoordinate, int pYCoordinate)
  {
    number = String.valueOf(pNumber);
    xCoordinate = pXCoordinate;
    yCoordinate = pYCoordinate;
  }

  /**
   * @return the actual number of the line represented as String
   */
  public String getNumber()
  {
    return number;
  }

  /**
   * @return y coordinate of the baseline of the number for the line
   */
  public int getYCoordinate()
  {
    return yCoordinate;
  }

  /**
   * @return x coordinate of the baseline of the number for the line
   */
  public int getXCoordinate()
  {
    return xCoordinate;
  }
}
