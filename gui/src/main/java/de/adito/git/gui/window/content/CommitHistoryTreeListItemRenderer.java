package de.adito.git.gui.window.content;

import de.adito.git.api.AncestryLine;
import de.adito.git.api.CommitHistoryTreeListItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that retrieves the component for the rendering of the CommitHistoryTreeListItems that form the
 * first row in the CommitHistory log
 *
 * @author m.kaspera 19.11.2018
 */
public class CommitHistoryTreeListItemRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof CommitHistoryTreeListItem) {
            CommitHistoryTreeListItem itemVal = (CommitHistoryTreeListItem) value;
            JPanel field = new JPanel(new BorderLayout());
            JComponent lineContainer = new CommitHistoryTreeListItemComponent(itemVal);
            JLabel shortMessageLabel = new JLabel(itemVal.getCommit().getShortMessage());
            lineContainer.setBackground(comp.getBackground());
            lineContainer.setForeground(comp.getForeground());
            lineContainer.setFont(comp.getFont());
            shortMessageLabel.setBackground(comp.getBackground());
            shortMessageLabel.setForeground(comp.getForeground());
            shortMessageLabel.setFont(comp.getFont());
            // needed so the background of the label is drawn if the line is selected
            shortMessageLabel.setOpaque(true);
            field.add(lineContainer, BorderLayout.WEST);
            field.add(shortMessageLabel, BorderLayout.CENTER);
            return field;
        }
        return comp;
    }

    /**
     * Class that does the actual rendering of the CommitHistoryTreeListItems in the paintComponent method
     */
    private class CommitHistoryTreeListItemComponent extends JPanel {

        private static final int LINE_SEPARATION = 20;
        private static final int LEFT_OFFSET = 10;
        private static final int RADIUS = 7;
        private final CommitHistoryTreeListItem commitHistoryTreeListItem;

        CommitHistoryTreeListItemComponent(CommitHistoryTreeListItem pCommitHistoryTreeListItem) {
            commitHistoryTreeListItem = pCommitHistoryTreeListItem;
            // needed so the line can be marked as selected
            setOpaque(true);
            setPreferredSize(new Dimension(_getNumLinesDrawn() * LINE_SEPARATION + 10, getHeight()));
        }

        /**
         * @return number of lines drawn, for determining the space the area with the lines need
         */
        private int _getNumLinesDrawn() {
            int counter = 0;
            // only count the children of the current commit once (check for false and set to true once the commit was encountered the first time)
            boolean encounteredCommit = false;
            for (int index = 0; index < commitHistoryTreeListItem.getAncestryLines().size(); index++) {
                if (commitHistoryTreeListItem.getAncestryLines().get(index).getLineType() != AncestryLine.LineType.STILLBORN
                        && commitHistoryTreeListItem.getAncestryLines().get(index).getParent().equals(commitHistoryTreeListItem.getCommit())) {
                    if (!encounteredCommit) {
                        // only count children that are not STILLBORN
                        counter += commitHistoryTreeListItem.getAncestryLines().get(index).getChildLines()
                                .stream()
                                .filter(childLine -> childLine.getLineType() != AncestryLine.LineType.STILLBORN)
                                .count();
                    }
                    encounteredCommit = true;
                    counter++;
                } else {
                    counter++;
                }
            }
            return counter;
        }

        @Override
        protected void paintComponent(Graphics g) {
            // If stillborn ancestry lines reference the current commit first, the knot index is not known yet -> store and draw them last
            List<AncestryLine> drawLater = new ArrayList<>();
            int knotIndex = Integer.MAX_VALUE;
            int numOfChildren = 0, numClosing = 0, numStillBorn = 0;
            super.paintComponent(g);
            for (int index = 0; index < commitHistoryTreeListItem.getAncestryLines().size(); index++) {
                g.setColor(commitHistoryTreeListItem.getAncestryLines().get(index).getColor());
                if (commitHistoryTreeListItem.getAncestryLines().get(index).getParent().equals(commitHistoryTreeListItem.getCommit())) {
                    // if true, the first reference to the current commit was found -> set knotIndex to currentIndex so all following references can point to the location of this line
                    if (knotIndex == Integer.MAX_VALUE) {
                        if (commitHistoryTreeListItem.getAncestryLines().get(index).getLineType() == AncestryLine.LineType.STILLBORN) {
                            drawLater.add(commitHistoryTreeListItem.getAncestryLines().get(index));
                            numStillBorn++;
                        } else {
                            knotIndex = index - numStillBorn;
                            // draw the dot/knot signaling the line/position of the comment in the particular line
                            _paintKnot(g, knotIndex);
                        /*
                         since we are in the line of the comment of this line, go through all children commits. This is the only place where
                         additional lines can branch off (which is the case if there is more than one child).
                         If the line just continues numOfChildren will be 1 after the for loop
                          */
                            for (AncestryLine childLine : commitHistoryTreeListItem.getAncestryLines().get(index).getChildLines()) {
                                // set color to color of child line
                                g.setColor(childLine.getColor());
                                if (childLine.getLineType() == AncestryLine.LineType.STILLBORN) {
                                    _paintLowerLine(g, knotIndex, childLine.getStillBornMeetingIndex());
                                } else {
                                    // draw line from the dot to the bottom of the cell, x value on the bottom is determined by the previous lines and the number of children before this one
                                    _paintLowerLine(g, knotIndex, knotIndex + numOfChildren);
                                    numOfChildren++;
                                }
                            }
                            // reset color since it could have been changed by the childLines
                            g.setColor(commitHistoryTreeListItem.getAncestryLines().get(index).getColor());
                            // draw line from top of the cell (at the incoming point of the line) to the dot/knot on the line that this particular commit is on
                            _paintUpperLine(g, knotIndex, knotIndex);
                        }
                    } else {
                        if (commitHistoryTreeListItem.getAncestryLines().get(index).getLineType() == AncestryLine.LineType.STILLBORN) {
                            _paintUpperLine(g, commitHistoryTreeListItem.getAncestryLines().get(index).getStillBornMeetingIndex(), knotIndex);
                            numStillBorn++;
                        } else {
                            numClosing++;
                            // reset color since it could have been changed by the childLines
                            g.setColor(commitHistoryTreeListItem.getAncestryLines().get(index).getColor());
                            // draw line from top of the cell (at the incoming point of the line) to the dot/knot on the line that this particular commit is on
                            _paintUpperLine(g, index, knotIndex);
                        }
                    }
                } else {
                    // draw straight line from the incoming top of the cell to the middle
                    _paintUpperLine(g, index - numStillBorn, index - numStillBorn);
                    // draw line from the middle of the cell to the bottom of the cell, x value is determined by the position that this line will be in in the next line
                    // influence of numOfChildren: if 1 or 0 same position, if n > 1 line will continue n - 1 lineIndices to the right
                    // influence of numClosing: this is the number of lines that will be gone in the next line, so line will be n lineIndices to the left
                    _paintLowerLine(g, index - numStillBorn, index + (numOfChildren > 0 ? numOfChildren - 1 : 0) - numClosing - numStillBorn);
                }
            }
            // if a STILLBORN line would have been drawn before any other line had referenced the commit in the current line, the STILLBORN line would not have
            // known where the knotIndex would be. That's why it is drawn in the end
            for (AncestryLine stillbornLine : drawLater) {
                g.setColor(stillbornLine.getColor());
                _paintUpperLine(g, stillbornLine.getStillBornMeetingIndex(), knotIndex);
            }
        }

        /**
         *
         * @param g Graphics object to draw with
         * @param pKnotIndex Index of the line that should have the knot/dot
         */
        private void _paintKnot(Graphics g, int pKnotIndex) {
            g.fillOval(LEFT_OFFSET + (pKnotIndex * LINE_SEPARATION) - RADIUS / 2, getHeight() / 2 - RADIUS / 2, RADIUS, RADIUS);
        }

        /**
         * draws a line with the specified x coordinates (determined from indices) from the middle of the cell to the lower part of the cell
         *
         * @param g            Graphics object to draw with
         * @param lineIndexTop index for the top of the line, determines the upper x Coordinate
         * @param lineIndexBot index for the bottom of the line, determines the lower x Coordinate
         */
        private void _paintLowerLine(Graphics g, double lineIndexTop, double lineIndexBot) {
            g.drawLine((int) (LEFT_OFFSET + lineIndexTop * LINE_SEPARATION), getHeight() / 2, (int) (LEFT_OFFSET + lineIndexBot * LINE_SEPARATION), getHeight());
        }

        /**
         * draws a line with the specified x coordinates (determined from indices) from the upper part of the cell to the middle of the cell
         *
         * @param g            Graphics object to draw with
         * @param lineIndexTop index for the top of the line, determines the upper x Coordinate
         * @param lineIndexBot index for the bottom of the line, determines the lower x Coordinate
         */
        private void _paintUpperLine(Graphics g, double lineIndexTop, double lineIndexBot) {
            g.drawLine((int) (LEFT_OFFSET + lineIndexTop * LINE_SEPARATION), 0, (int) (LEFT_OFFSET + lineIndexBot * LINE_SEPARATION), getHeight() / 2);
        }
    }
}
