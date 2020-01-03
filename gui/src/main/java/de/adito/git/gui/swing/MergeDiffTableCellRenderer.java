package de.adito.git.gui.swing;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IMergeDiff;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Renderer that colors the columns of a table with IMergeDiffs according to the following schema:
 * FilePath/Name are colored white if the changeType is different for the two sides. If they are the same, they are colored the same way as the changeTypes
 * Columnm 3 and 4, containing the changeTypes for the YOURS and THEIRS version of the conflict are always colored according to their changeType
 *
 * If the changeType for a version is RENAME, the new file name is also written behind the changeType
 *
 * @author m.kaspera, 11.11.2019
 */
public class MergeDiffTableCellRenderer extends DefaultTableCellRenderer
{

  @Override
  public Component getTableCellRendererComponent(JTable pTable, Object pValue, boolean pIsSelected, boolean pHasFocus, int pRowIndex, int pColumnIndex)
  {
    IMergeDiff mergeDiff = (IMergeDiff) pValue;
    // Text in cells is always displayed as label
    JLabel label = new JLabel(_getStringRepresenation(mergeDiff, pColumnIndex));
    if (!pIsSelected)
    {
      Color foreground = _getForeground(mergeDiff, pColumnIndex);
      if (foreground != null)
        label.setForeground(foreground);
    }
    else
      label = (JLabel) super.getTableCellRendererComponent(pTable, _getStringRepresenation(mergeDiff, pColumnIndex), pIsSelected, pHasFocus, pRowIndex, pColumnIndex);
    return label;
  }

  /**
   * Determines the color with which the text for the given column should be in
   *
   * @param pMergeDiff   IMergeDiff with informations about a conflicting file
   * @param pColumnIndex index, determines which information is used to determine the color
   * @return Color to be used as foreground color for the given column
   */
  private static Color _getForeground(IMergeDiff pMergeDiff, int pColumnIndex)
  {
    if (pColumnIndex <= 1)
    {
      if (pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileHeader().getChangeType() ==
          pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS).getFileHeader().getChangeType())
      {
        return pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileHeader().getChangeType().getStatusColor();
      }
      else
      {
        return null;
      }
    }
    else if (pColumnIndex == 2)
    {
      return pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileHeader().getChangeType().getStatusColor();
    }
    else if (pColumnIndex == 3)
    {
      return pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS).getFileHeader().getChangeType().getStatusColor();
    }
    return null;
  }

  /**
   * Builds the string representation of the IMergeDiff for the given column
   *
   * @param pMergeDiff   IMergeDiff with informations about a conflicting file
   * @param pColumnIndex index, determines which information of the IMergeDiff are packed into the string
   * @return String representation of information about the IMergeDiff, dependend on the passed column
   */
  private static String _getStringRepresenation(IMergeDiff pMergeDiff, int pColumnIndex)
  {
    Path path = Paths.get(pMergeDiff.getFilePath());
    if (pColumnIndex == 0)
    {
      return path.getFileName().toString();
    }
    else if (pColumnIndex == 1)
    {
      return path.subpath(0, Math.max(1, path.getNameCount() - 1)).toString();
    }
    else if (pColumnIndex == 2)
    {
      return _getChangeTypeDescription(pMergeDiff, IMergeDiff.CONFLICT_SIDE.YOURS);
    }
    else if (pColumnIndex == 3)
    {
      return _getChangeTypeDescription(pMergeDiff, IMergeDiff.CONFLICT_SIDE.THEIRS);
    }
    return "No Data";
  }

  /**
   * Returns a String with the type and, in the case of a rename, the new name of the file
   *
   * @param pMergeDiff    MergeDiff with infos about the merge and the change to the file
   * @param pConflictSide which side of the mergeConflict should be used
   * @return Description of the changeType
   */
  private static String _getChangeTypeDescription(IMergeDiff pMergeDiff, IMergeDiff.CONFLICT_SIDE pConflictSide)
  {
    EChangeType changeType = pMergeDiff.getDiff(pConflictSide).getFileHeader().getChangeType();
    if (changeType == EChangeType.RENAME)
    {
      return changeType.toString() + " [" + pMergeDiff.getDiff(pConflictSide).getFileHeader().getFilePath(EChangeSide.NEW) + "]";
    }
    return changeType.toString();
  }

}
