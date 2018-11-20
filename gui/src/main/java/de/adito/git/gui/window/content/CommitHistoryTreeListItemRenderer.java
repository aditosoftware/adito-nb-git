package de.adito.git.gui.window.content;

import de.adito.git.api.AncestryLine;
import de.adito.git.api.CommitHistoryTreeListItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

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
            JComponent container = new CommitHistoryTreeListItemComponent(itemVal);
            container.setBackground(comp.getBackground());
            container.setForeground(comp.getForeground());
            container.setFont(comp.getFont());
            field.add(container, BorderLayout.WEST);
            field.add(new JLabel(itemVal.getCommit().getShortMessage()), BorderLayout.CENTER);
            return field;
        }
        return comp;
    }

    /**
     * Class that does the actual rendering of the CommitHistoryTreeListItems in the paintComponent method
     */
    private class CommitHistoryTreeListItemComponent extends JPanel {

        private static final int radius = 7;
        private static final int leftOffset = 10;
        private final CommitHistoryTreeListItem commitHistoryTreeListItem;

        CommitHistoryTreeListItemComponent(CommitHistoryTreeListItem pCommitHistoryTreeListItem) {
            commitHistoryTreeListItem = pCommitHistoryTreeListItem;
            setOpaque(true);
            setPreferredSize(new Dimension(_getNumLinesDrawn() * 20 + 10, getHeight()));
        }

        private int _getNumLinesDrawn() {
            int counter = 0;
            boolean encounteredCommit = false;
            for (int index = 0; index < commitHistoryTreeListItem.getAncestryLines().size(); index++) {
                if (commitHistoryTreeListItem.getAncestryLines().get(index).getParent().equals(commitHistoryTreeListItem.getCommit())) {
                    if (!encounteredCommit) {
                        counter += commitHistoryTreeListItem.getAncestryLines().get(index).getChildLines().size();
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
            int knotIndex = Integer.MAX_VALUE;
            int numOfChildren = 0, numClosing = 0;
            super.paintComponent(g);
            for (int index = 0; index < commitHistoryTreeListItem.getAncestryLines().size(); index++) {
                g.setColor(commitHistoryTreeListItem.getAncestryLines().get(index).getColor());
                if (commitHistoryTreeListItem.getAncestryLines().get(index).getParent().equals(commitHistoryTreeListItem.getCommit())) {
                    if (knotIndex == Integer.MAX_VALUE) {
                        knotIndex = index;
                        // draw the dot/knot signaling the line/position of the comment in the particular line
                        g.fillOval(leftOffset + (index * 20) - radius / 2, getHeight() / 2 - radius / 2, radius, radius);
                        /*
                         since we are in the line of the comment of this line, go through all children commits. This is the only place where
                         additional lines can branch off (which is the case if there is more than one child).
                         If the line just continues numOfChildren will be 1 after the for loop
                          */
                        for (AncestryLine childLine : commitHistoryTreeListItem.getAncestryLines().get(index).getChildLines()) {
                            // set color to color of child line
                            g.setColor(childLine.getColor());
                            // draw line from the dot to the bottom of the cell, x value on the bottom is determined by the previous lines and the number of children before this one
                            g.drawLine(leftOffset + index * 20, getHeight() / 2, leftOffset + (index + numOfChildren) * 20, getHeight());
                            numOfChildren++;
                        }
                    } else {
                        numClosing++;
                    }
                    // reset color since it could have been changed by the childLines
                    g.setColor(commitHistoryTreeListItem.getAncestryLines().get(index).getColor());
                    // draw line from top of the cell (at the incoming point of the line) to the dot/knot on the line that this particular commit is on
                    g.drawLine(leftOffset + index * 20, 0, leftOffset + knotIndex * 20, getHeight() / 2);
                } else {
                    // draw straight line from the incoming top of the cell to the middle
                    g.drawLine(leftOffset + index * 20, 0, leftOffset + index * 20, getHeight() / 2);
                    // draw line from the middle of the cell to the bottom of the cell, x value is determined by the position that this line will be in in the next line
                    // influence of numOfChildren: if 1 or 0 same position, if n > 1 line will continue n - 1 lineIndices to the right
                    // influence of numClosing: this is the number of lines that will be gone in the next line, so line will be n lineIndices to the left
                    g.drawLine(leftOffset + index * 20, getHeight() / 2, leftOffset + (index + (numOfChildren > 0 ? numOfChildren - 1 : 0) - numClosing) * 20, getHeight());
                }
            }
        }
    }
}
