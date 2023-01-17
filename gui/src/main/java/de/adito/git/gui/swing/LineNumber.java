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
  private final int height;

  /**
   * @param pNumber      the actual number of the line
   * @param pXCoordinate x coordinate of the baseline of the number for the line
   * @param pYCoordinate y coordinate of the baseline of the number for the line
   * @param pHeight      how high is this Line?
   */
  public LineNumber(int pNumber, int pXCoordinate, int pYCoordinate, int pHeight)
  {
    number = String.valueOf(pNumber);
    xCoordinate = pXCoordinate;
    yCoordinate = pYCoordinate;
    height = pHeight;
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

  /**
   * @return height of the line this object represents
   */
  public int getHeight()
  {
    return height;
  }
}
