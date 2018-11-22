package de.adito.git.gui.window.content;

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

        private final static int PADDING_RIGHT = 20;
        private final CommitHistoryTreeListItem commitHistoryTreeListItem;

        CommitHistoryTreeListItemComponent(CommitHistoryTreeListItem pCommitHistoryTreeListItem) {
            commitHistoryTreeListItem = pCommitHistoryTreeListItem;
            // needed so the line can be marked as selected
            setOpaque(true);
            setPreferredSize(new Dimension(commitHistoryTreeListItem.getMaxLineWidth() + PADDING_RIGHT, getHeight()));
        }

        @Override
        protected void paintComponent(Graphics g) {
            // call this for a working selection marker
            super.paintComponent(g);
            // draw all the lines that belong to the CommitHistoryTreeListItem
            for (CommitHistoryTreeListItem.ColoredLineCoordinates coloredLineCoordinate : commitHistoryTreeListItem.getLinesToDraw()) {
                g.setColor(coloredLineCoordinate.getColor());
                if (coloredLineCoordinate.isUpperPart()) {
                    _paintUpperLine(g, coloredLineCoordinate.getX1(), coloredLineCoordinate.getX2());
                } else {
                    _paintLowerLine(g, coloredLineCoordinate.getX1(), coloredLineCoordinate.getX2());
                }
            }
            // finally draw the Knot that symbolizes the current commit, drawn last so it is not covered by anything
            g.setColor(commitHistoryTreeListItem.getKnotCoordinates().getColor());
            _paintKnot(g, commitHistoryTreeListItem.getKnotCoordinates().getXCoordinate());
        }

        /**
         * @param g       Graphics object to draw with
         * @param pXValue Value of the xCoordinate for the upper right edge of the oval
         */
        private void _paintKnot(Graphics g, int pXValue) {
            g.fillOval(pXValue, getHeight() / 2 - CommitHistoryTreeListItem.KnotCoordinates.RADIUS / 2,
                    CommitHistoryTreeListItem.KnotCoordinates.RADIUS, CommitHistoryTreeListItem.KnotCoordinates.RADIUS);
        }

        /**
         * draws a line with the specified x coordinates (determined from indices) from the middle of the cell to the lower part of the cell
         *
         * @param g       Graphics object to draw with
         * @param xValTop the upper x Coordinate
         * @param xValBot the lower x Coordinate
         */
        private void _paintLowerLine(Graphics g, int xValTop, int xValBot) {
            g.drawLine(xValTop, getHeight() / 2, xValBot, getHeight());
        }

        /**
         * draws a line with the specified x coordinates (determined from indices) from the upper part of the cell to the middle of the cell
         *
         * @param g       Graphics object to draw with
         * @param xValTop the upper x Coordinate
         * @param xValBot the lower x Coordinate
         */
        private void _paintUpperLine(Graphics g, int xValTop, int xValBot) {
            g.drawLine(xValTop, 0, xValBot, getHeight() / 2);
        }
    }
}
