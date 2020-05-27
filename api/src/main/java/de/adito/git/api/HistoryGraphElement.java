package de.adito.git.api;

import de.adito.git.api.data.ICommit;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to calculate and store information about how the AncestryLines are to be drawn
 *
 * @author m.kaspera, 24.05.2019
 */
public class HistoryGraphElement
{

  private KnotCoordinates knotCoordinates = null;
  private final List<ColoredLineCoordinates> lineCoordinates = new ArrayList<>();
  private int knotIndex = Integer.MAX_VALUE;

  /**
   * calculates and stores the lines to draw leading up to the current commit (top half of the cell/element)
   *
   * @param pAncestryLines AncestryLines as they are before encountering the current commit
   * @param pAdvancedLine  the line that leads up to the current commit. Needed for the color of the knot in case it's a new branch
   * @param pCurrentCommit the current commit for this element/cell
   */
  public void calculateUpperLines(List<AncestryLine> pAncestryLines, AncestryLine pAdvancedLine, ICommit pCurrentCommit)
  {
    List<AncestryLine> calculateLater = new ArrayList<>();
    int numClosing = 0, numStillborn = 0;
    for (int index = 0; index < pAncestryLines.size(); index++)
    {
      if (pAncestryLines.get(index).getLineType() != AncestryLine.LineType.EMPTY)
      {
        if (pAncestryLines.get(index).getNextCommit().equals(pCurrentCommit))
        {
          // if true, the first reference to the current commit was found -> set knotIndex to currentIndex so all following references can point
          // to the location of this line
          if (knotIndex == Integer.MAX_VALUE)
          {
            if (pAncestryLines.get(index).getLineType() == AncestryLine.LineType.STILLBORN)
            {
              calculateLater.add(pAncestryLines.get(index));
              numStillborn++;
            }
            else
            {
              knotIndex = index - numStillborn - numClosing;
              knotCoordinates = new KnotCoordinates(
                  ColoredLineCoordinates.LEFT_OFFSET + (knotIndex * ColoredLineCoordinates.LINE_SEPARATION) - KnotCoordinates.RADIUS / 2,
                  pAncestryLines.get(index).getColor());
              lineCoordinates.add(_getCoordinatesForIndices((double) index - numStillborn, knotIndex, true, pAncestryLines.get(index).getColor()));
            }
          }
          else
          {
            if (pAncestryLines.get(index).getLineType() == AncestryLine.LineType.STILLBORN)
            {
              lineCoordinates.add(_getCoordinatesForIndices(pAncestryLines.get(index).getStillBornMeetingIndex(), knotIndex, true,
                                                            pAncestryLines.get(index).getColor()));
              numStillborn++;
            }
            else
            {
              numClosing++;
              // draw line from top of the cell (at the incoming point of the line) to the dot/knot on the line that this particular commit is on
              lineCoordinates.add(_getCoordinatesForIndices((double) index - numStillborn, knotIndex, true, pAncestryLines.get(index).getColor()));
            }
          }
        }
        else
        {
          // draw straight line from the incoming top of the cell to the middle
          lineCoordinates.add(_getCoordinatesForIndices((double) index - numStillborn, (double) index - numStillborn - numClosing,
                                                        true, pAncestryLines.get(index).getColor()));
        }
      }
    }
    // if a STILLBORN line would have been drawn before any other line had referenced the commit in the current line, the STILLBORN line would
    // not have known where the knotIndex would be. That's why it is drawn in the end
    for (AncestryLine stillbornLine : calculateLater)
    {
      lineCoordinates.add(_getCoordinatesForIndices(stillbornLine.getStillBornMeetingIndex(), knotIndex, true, stillbornLine.getColor()));
    }
    if (knotIndex == Integer.MAX_VALUE)
    {
      knotIndex = pAncestryLines.size() - numClosing - numStillborn;
      knotCoordinates = new KnotCoordinates(
          ColoredLineCoordinates.LEFT_OFFSET + (knotIndex * ColoredLineCoordinates.LINE_SEPARATION) - KnotCoordinates.RADIUS / 2,
          pAdvancedLine.getColor());
    }
  }

  /**
   * calculates and stores the lines to draw for the AncestryLines as they are after the commit of this element of the graph (means in the lower half of the table cell)
   *
   * @param pAncestryLines list of AncestryLines as they are after encountering the current commit
   */
  public void calculateLowerLines(List<AncestryLine> pAncestryLines)
  {
    int numClosing = 0, numOpening = 0, numStillborn = 0;
    if (knotIndex == Integer.MAX_VALUE)
    {
      return;
    }
    for (int index = 0; index < pAncestryLines.size(); index++)
    {
      if (pAncestryLines.get(index).getLineType() != AncestryLine.LineType.EMPTY)
      {
        if (pAncestryLines.get(index).getLineType() == AncestryLine.LineType.INFANT)
        {
          lineCoordinates.add(_getCoordinatesForIndices(knotIndex, (double) index - numStillborn, false, pAncestryLines.get(index).getColor()));
          numOpening++;
        }
        else if (pAncestryLines.get(index).getLineType() == AncestryLine.LineType.STILLBORN)
        {
          lineCoordinates.add(_getCoordinatesForIndices(knotIndex, pAncestryLines.get(index).getStillBornMeetingIndex(), false, pAncestryLines.get(index).getColor()));
          numStillborn++;
        }
        else
        {
          // draw straight line from the incoming top of the cell to the middle
          lineCoordinates.add(_getCoordinatesForIndices((double) index - numStillborn - numOpening, (double) index - numStillborn - numClosing,
                                                        false, pAncestryLines.get(index).getColor()));
        }
      }
    }
  }

  KnotCoordinates getKnotCoordinates()
  {
    return knotCoordinates;
  }

  List<ColoredLineCoordinates> getLineCoordinates()
  {
    return lineCoordinates;
  }


  /**
   * @return the maximum x-value of any of the lines. This is the minimum width that the lines need
   */
  int calculateMaxLineWidth()
  {
    int tmpMax = knotCoordinates.xCoordinate;
    for (ColoredLineCoordinates lineCoordinate : lineCoordinates)
    {
      if (lineCoordinate.getX1() > tmpMax)
        tmpMax = lineCoordinate.getX1();
      if (lineCoordinate.getX2() > tmpMax)
        tmpMax = lineCoordinate.getX2();
    }
    return tmpMax;
  }

  private ColoredLineCoordinates _getCoordinatesForIndices(double pIndexStart, double pIndexEnd, boolean pUpperPart, Color pColor)
  {
    return new ColoredLineCoordinates((int) (ColoredLineCoordinates.LEFT_OFFSET + pIndexStart * ColoredLineCoordinates.LINE_SEPARATION),
                                      (int) (ColoredLineCoordinates.LEFT_OFFSET + pIndexEnd * ColoredLineCoordinates.LINE_SEPARATION),
                                      pUpperPart, pColor);
  }

  /**
   * Class for storing information about the position/color of the
   * knot that symbolizes the current commit. Only contains the x-value
   * of the upper left corner for the oval, calculate the rest by using
   * the height of the cell that the knot is drawn in and the radius of the circle
   */
  public static class KnotCoordinates
  {

    public static final int RADIUS = 9;
    private final int xCoordinate;
    private final Color color;

    KnotCoordinates(int pXCoordinate, Color pColor)
    {
      xCoordinate = pXCoordinate;
      color = pColor;
    }

    /**
     * @return x-Value for the upper left corner of the oval that forms the filled circle
     */
    public int getXCoordinate()
    {
      return xCoordinate;
    }

    /**
     * @return Color that the circle/knot should be drawn in
     */
    public Color getColor()
    {
      return color;
    }
  }

  /**
   * Symbolizes a line with color, only has the two x-Coordinates of the line though
   * The y-Coordinates are to be determined by the height of the cell the line is drawn
   * in and the change of the upperPart boolean (if true draw from top of the cell to middle,
   * if false draw from middle to lower part of cell)
   */
  public static class ColoredLineCoordinates
  {

    public static final int LINE_WIDTH = 2;
    private static final int LEFT_OFFSET = 10;
    private static final int LINE_SEPARATION = 20;

    private final int x1;
    private final int x2;
    private final boolean upperPart;
    private final Color color;

    ColoredLineCoordinates(int pX1, int pX2, boolean pUpperPart, Color pColor)
    {
      x1 = pX1;
      x2 = pX2;
      upperPart = pUpperPart;
      color = pColor;
    }

    /**
     * @return x-Value of the starting point of the line
     */
    public int getX1()
    {
      return x1;
    }

    /**
     * @return x-Value of the ending point of the line
     */
    public int getX2()
    {
      return x2;
    }

    /**
     * @return does the line go from the upper part of a cell to the middle?
     */
    public boolean isUpperPart()
    {
      return upperPart;
    }

    /**
     * @return Color that the line should be drawn in
     */
    public Color getColor()
    {
      return color;
    }
  }
}
