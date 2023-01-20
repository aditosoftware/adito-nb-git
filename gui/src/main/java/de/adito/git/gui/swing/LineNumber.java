package de.adito.git.gui.swing;

import lombok.Getter;

/**
 * Contains the line number and the y Coordinate for the baseline of the line number when drawn on the panel
 *
 * @author m.kaspera, 10.01.2019
 */
public class LineNumber
{

  @Getter
  private final String number;
  @Getter
  private final int yCoordinate;
  @Getter
  private final int xCoordinate;
  @Getter
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
}
