package de.adito.git.api;


import de.adito.git.api.data.ICommit;
import lombok.NonNull;

import java.awt.Color;

/**
 * Class that symbolizes one line in the history view of commits.
 * Stores the line color and the next nextCommit commit in that line
 *
 * @author m.kaspera 16.11.2018
 */
public class AncestryLine
{

  private final Color color;
  private LineType lineType;
  private double stillBornMeetingIndex = 0;
  private final ICommit nextCommit;

  /**
   * FULL: active line
   * INFANT: line that has yet to spawn
   * STILLBORN: line that will spawn and be gone in the very next line (usually a merge, but not all merges are STILLBORN)
   * EMPTX: line that ended at the node above, just here to keep the spacing the way it needs to be for the next cell
   */
  public enum LineType
  {
    FULL, INFANT, STILLBORN, EMPTY
  }

  /**
   * create a new AncestryLine object with the specified attributes
   *
   * @param pNextCommit ICommit that is the next commit in the line symbolized by this class
   * @param pColor      Color of the line
   * @param pLineType   LineType, INFANT for unborn lines, FULL for lines that are already active in the row of nextCommit
   */
  public AncestryLine(@NonNull ICommit pNextCommit, @NonNull Color pColor, @NonNull LineType pLineType)
  {
    nextCommit = pNextCommit;
    color = pColor;
    lineType = pLineType;
  }

  /**
   * @return the next ICommit in the line
   */
  public ICommit getNextCommit()
  {
    return nextCommit;
  }

  /**
   * @return the color of the line
   */
  public Color getColor()
  {
    return color;
  }

  /**
   * @return LineType, FULL for an already active line, INFANT for an yet unborn line and STILLBORN for a line
   * will spawn and be gone in the very next row
   */
  public LineType getLineType()
  {
    return lineType;
  }

  /**
   * @return index where the two parts of a stillborn line meet. 0 for all other types of AncestryLines
   */
  double getStillBornMeetingIndex()
  {
    return stillBornMeetingIndex;
  }

  public void setStillBornMeetingIndex(double pStillBornMeetingIndex)
  {
    stillBornMeetingIndex = pStillBornMeetingIndex;
  }

  public void setLineType(LineType pLineType)
  {
    lineType = pLineType;
  }
}
