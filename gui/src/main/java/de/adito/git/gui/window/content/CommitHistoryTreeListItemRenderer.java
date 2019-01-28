package de.adito.git.gui.window.content;

import de.adito.git.api.CommitHistoryTreeListItem;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Class that retrieves the component for the rendering of the CommitHistoryTreeListItems that form the
 * first row in the CommitHistory log
 *
 * @author m.kaspera 19.11.2018
 */
public class CommitHistoryTreeListItemRenderer extends DefaultTableCellRenderer
{

  private final ImageIcon tagIcon;
  private final ImageIcon stashIcon;
  private final ImageIcon localBranchIcon;
  private final ImageIcon headIcon;
  private final ImageIcon originBranchIcon;

  CommitHistoryTreeListItemRenderer()
  {
    tagIcon = new ImageIcon(getClass().getResource(Constants.TAG_ICON));
    localBranchIcon = new ImageIcon(getClass().getResource(Constants.BRANCH_ICON_LOCAL));
    headIcon = new ImageIcon(getClass().getResource(Constants.BRANCH_ICON_HEAD));
    originBranchIcon = new ImageIcon(getClass().getResource(Constants.BRANCH_ICON_ORIGIN));
    stashIcon = new ImageIcon(getClass().getResource(Constants.STASH_COMMIT_ICON));
  }

  @Override
  public Component getTableCellRendererComponent(JTable pTable, Object pValue, boolean pIsSelected,
                                                 boolean pHasFocus, int pRow, int pColumn)
  {
    Component comp = super.getTableCellRendererComponent(pTable, pValue, pIsSelected, pHasFocus, pRow, pColumn);
    if (pValue instanceof CommitHistoryTreeListItem)
    {
      CommitHistoryTreeListItem itemVal = (CommitHistoryTreeListItem) pValue;
      JPanel field = new JPanel(new BorderLayout());
      JComponent lineContainer = new CommitHistoryTreeListItemComponent(itemVal);
      JLabel shortMessageLabel = new JLabel(itemVal.getCommit().getShortMessage());
      JPanel branchTags = new _BranchTagPanel(itemVal.getBranches());
      lineContainer.setBackground(comp.getBackground());
      lineContainer.setForeground(comp.getForeground());
      lineContainer.setFont(comp.getFont());
      shortMessageLabel.setBackground(comp.getBackground());
      shortMessageLabel.setForeground(comp.getForeground());
      branchTags.setBackground(comp.getBackground());
      branchTags.setForeground(comp.getForeground());
      shortMessageLabel.setFont(comp.getFont());
      // needed so the background of the label is drawn if the line is selected
      shortMessageLabel.setOpaque(true);
      branchTags.setOpaque(true);
      field.add(branchTags, BorderLayout.EAST);
      field.add(lineContainer, BorderLayout.WEST);
      field.add(shortMessageLabel, BorderLayout.CENTER);
      return field;
    }
    return comp;
  }

  /**
   * Class that does the actual rendering of the CommitHistoryTreeListItems in the paintComponent method
   */
  private class CommitHistoryTreeListItemComponent extends JPanel
  {

    private static final int PADDING_RIGHT = 20;
    private final CommitHistoryTreeListItem commitHistoryTreeListItem;

    CommitHistoryTreeListItemComponent(CommitHistoryTreeListItem pCommitHistoryTreeListItem)
    {
      commitHistoryTreeListItem = pCommitHistoryTreeListItem;
      // needed so the line can be marked as selected
      setOpaque(true);
      setPreferredSize(new Dimension(commitHistoryTreeListItem.getMaxLineWidth() + PADDING_RIGHT, getHeight()));
    }

    @Override
    protected void paintComponent(Graphics pG)
    {
      // call this for a working selection marker
      super.paintComponent(pG);
      Graphics2D g2d = (Graphics2D) pG;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setStroke(new BasicStroke(CommitHistoryTreeListItem.ColoredLineCoordinates.LINE_WIDTH));
      // draw all the lines that belong to the CommitHistoryTreeListItem
      for (CommitHistoryTreeListItem.ColoredLineCoordinates coloredLineCoordinate : commitHistoryTreeListItem.getLinesToDraw())
      {
        pG.setColor(coloredLineCoordinate.getColor());
        if (coloredLineCoordinate.isUpperPart())
        {
          _paintUpperLine(pG, coloredLineCoordinate.getX1(), coloredLineCoordinate.getX2());
        }
        else
        {
          _paintLowerLine(pG, coloredLineCoordinate.getX1(), coloredLineCoordinate.getX2());
        }
      }
      // finally draw the Knot that symbolizes the current commit, drawn last so it is not covered by anything
      pG.setColor(commitHistoryTreeListItem.getKnotCoordinates().getColor());
      _paintKnot(pG, commitHistoryTreeListItem.getKnotCoordinates().getXCoordinate());
    }

    /**
     * @param pG      Graphics object to draw with
     * @param pXValue Value of the xCoordinate for the upper right edge of the oval
     */
    private void _paintKnot(Graphics pG, int pXValue)
    {
      pG.fillOval(pXValue, getHeight() / 2 - CommitHistoryTreeListItem.KnotCoordinates.RADIUS / 2,
                  CommitHistoryTreeListItem.KnotCoordinates.RADIUS, CommitHistoryTreeListItem.KnotCoordinates.RADIUS);
    }

    /**
     * draws a line with the specified x coordinates (determined from indices) from the middle of the cell to the lower part of the cell
     *
     * @param pG       Graphics object to draw with
     * @param pXValTop the upper x Coordinate
     * @param pXValBot the lower x Coordinate
     */
    private void _paintLowerLine(Graphics pG, int pXValTop, int pXValBot)
    {
      pG.drawLine(pXValTop, getHeight() / 2, pXValBot, getHeight());
    }

    /**
     * draws a line with the specified x coordinates (determined from indices) from the upper part of the cell to the middle of the cell
     *
     * @param pG       Graphics object to draw with
     * @param pXValTop the upper x Coordinate
     * @param pXValBot the lower x Coordinate
     */
    private void _paintUpperLine(Graphics pG, int pXValTop, int pXValBot)
    {
      pG.drawLine(pXValTop, 0, pXValBot, getHeight() / 2);
    }
  }

  private class _BranchTagPanel extends JPanel
  {

    private static final int ICON_SEPARATION = 6;
    private static final int MARGIN_RIGHT = 3;
    private static final int MARGIN_ICONS_TEXT = 5;
    private final java.util.List<IBranch> branches;
    private String branchString = "";

    _BranchTagPanel(List<IBranch> pBranches)
    {
      branches = pBranches;
      if (!branches.isEmpty())
      {
        StringBuilder toolTipBuilder = new StringBuilder("<html>");
        StringBuilder textBuilder = new StringBuilder();
        for (IBranch branch : branches)
        {
          toolTipBuilder.append(branch.getSimpleName()).append("<br>");
          textBuilder.append(branch.getSimpleName()).append(" & ");
        }
        toolTipBuilder.append("</html>");
        setToolTipText(toolTipBuilder.toString());
        branchString = textBuilder.delete(textBuilder.length() - 3, textBuilder.length() - 1).toString();
        setPreferredSize(new Dimension(getFontMetrics(getFont()).stringWidth(branchString)
                                           + ((branches.size() - 1) * ICON_SEPARATION + localBranchIcon.getIconWidth())
                                           + MARGIN_ICONS_TEXT + MARGIN_RIGHT,
                                       localBranchIcon.getIconHeight()));
      }
    }

    @Override
    protected void paintComponent(Graphics pGraphics)
    {
      super.paintComponent(pGraphics);
      ((Graphics2D) pGraphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      for (int index = branches.size() - 1; index >= 0; index--)
      {
        int yCoordinate = (getHeight() - localBranchIcon.getIconHeight()) / 2;
        IBranch currentBranch = branches.get(index);
        if (currentBranch.getType() == EBranchType.LOCAL)
          localBranchIcon.paintIcon(this, pGraphics, (index) * ICON_SEPARATION, yCoordinate);
        else if (currentBranch.getType() == EBranchType.REMOTE)
          originBranchIcon.paintIcon(this, pGraphics, (index) * ICON_SEPARATION, yCoordinate);
        else if (currentBranch.getType() == EBranchType.EMPTY)
        {
          if (currentBranch.getName().contains("HEAD"))
          {
            headIcon.paintIcon(this, pGraphics, (index) * ICON_SEPARATION, yCoordinate);
          }
          else if ("stash".equals(currentBranch.getSimpleName()))
          {
            stashIcon.paintIcon(this, pGraphics, (index) * ICON_SEPARATION, yCoordinate);
          }
        }
      }
      pGraphics.drawString(branchString, (branches.size() - 1) * ICON_SEPARATION + localBranchIcon.getIconWidth() + MARGIN_ICONS_TEXT,
                           getFontMetrics(getFont()).getAscent() + (getHeight() - getFontMetrics(getFont()).getHeight()) / 2);
    }
  }
}
